package com.uvstudio.him.photofilterlibrary

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.uvstudio.him.photofilterlibrary.sample.R
import com.uvstudio.him.photofilterlibrary.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var filterAdapter: FilterAdapter

    private var originalBitmap: Bitmap? = null
    private var thumbnailSourceBitmap: Bitmap? = null

    private var activeFilter: FilterType = FilterType.GRAYSCALE
    private var filterIntensity: Float = 1.0f

    private var brightness: Float = 0.0f
    private var contrast: Float = 1.0f
    private var warmth: Float = 0.0f
    private var includeDateStamp: Boolean = false

    private var currentCategory: FilterCategory = FilterCategory.ALL
    private var renderJob: Job? = null

    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { loadBitmapFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupCategoryTabs()
        setupControls()
        loadDefaultImage()
    }

    private fun setupRecyclerView() {
        filterAdapter = FilterAdapter { selectedFilter ->
            activeFilter = selectedFilter
            applyCurrentPipeline()
        }
        binding.rvFilters.adapter = filterAdapter
    }

    private fun setupCategoryTabs() {
        for (category in FilterCategory.entries) {
            val tab = binding.tabLayoutCategories.newTab().setText(category.displayName)
            binding.tabLayoutCategories.addTab(tab)
        }

        binding.tabLayoutCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val index = tab?.position ?: 0
                currentCategory = FilterCategory.entries.getOrElse(index) { FilterCategory.ALL }
                refreshThumbnails()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupControls() {
        // Intensity Slider
        binding.sliderIntensity.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                filterIntensity = value / 100f
                binding.txtIntensityLabel.text = "Strength: ${value.toInt()}%"
                applyCurrentPipeline()
            }
        }

        // Adjustments Toggle
        binding.btnToggleAdjustments.setOnClickListener {
            val isVisible = binding.panelAdjustments.visibility == View.VISIBLE
            binding.panelAdjustments.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        // Adjustments Sliders
        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                brightness = value / 100f
                binding.txtBrightness.text = "Brightness: ${(brightness * 100).toInt()}"
                applyCurrentPipeline()
            }
        }

        binding.sliderContrast.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                contrast = value
                binding.txtContrast.text = String.format("Contrast: %.1f", contrast)
                applyCurrentPipeline()
            }
        }

        binding.sliderWarmth.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                warmth = value / 100f
                binding.txtWarmth.text = "Warmth: ${(warmth * 100).toInt()}"
                applyCurrentPipeline()
            }
        }

        // Retro Date Stamp Chip
        binding.chipDateStamp.setOnCheckedChangeListener { _, isChecked ->
            includeDateStamp = isChecked
            applyCurrentPipeline()
        }

        // Flip Button
        binding.btnFlip.setOnClickListener {
            originalBitmap?.let { bmp ->
                setImage(bmp.flipHorizontal())
            }
        }

        // Rotate Button
        binding.btnRotate.setOnClickListener {
            originalBitmap?.let { bmp ->
                setImage(bmp.rotate(90f))
            }
        }

        // Reset Adjustments
        binding.btnResetAdjustments.setOnClickListener {
            brightness = 0f
            contrast = 1f
            warmth = 0f
            includeDateStamp = false
            binding.chipDateStamp.isChecked = false
            binding.sliderBrightness.value = 0f
            binding.sliderContrast.value = 1f
            binding.sliderWarmth.value = 0f
            binding.txtBrightness.text = "Brightness: 0"
            binding.txtContrast.text = "Contrast: 1.0"
            binding.txtWarmth.text = "Warmth: 0"
            applyCurrentPipeline()
        }

        // Top App Bar Buttons
        binding.btnPickPhoto.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnSave.setOnClickListener {
            saveFilteredImageToGallery()
        }

        binding.btnShare.setOnClickListener {
            shareFilteredImage()
        }
    }

    private fun loadDefaultImage() {
        val original = BitmapFactory.decodeResource(resources, R.drawable.photo)
        setImage(original)
    }

    private fun loadBitmapFromUri(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    withContext(Dispatchers.Main) {
                        setImage(bitmap)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setImage(bitmap: Bitmap) {
        this.originalBitmap = bitmap

        // Create downscaled thumbnail for generating carousel previews fast
        val thumbSize = 120
        this.thumbnailSourceBitmap = Bitmap.createScaledBitmap(
            bitmap,
            thumbSize,
            (thumbSize * (bitmap.height.toFloat() / bitmap.width)).toInt(),
            true
        )

        binding.splitSliderView.setBitmaps(bitmap, bitmap)
        refreshThumbnails()
        applyCurrentPipeline()
    }

    private fun refreshThumbnails() {
        val thumbSource = thumbnailSourceBitmap ?: return
        val filters = FilterType.getByCategory(currentCategory)

        lifecycleScope.launch(Dispatchers.Default) {
            val items = filters.map { filter ->
                val thumb = FilterEngine.applyFilter(thumbSource, filter, 1.0f)
                FilterThumbnailItem(
                    filterType = filter,
                    thumbnailBitmap = thumb,
                    isSelected = (filter == activeFilter)
                )
            }
            withContext(Dispatchers.Main) {
                filterAdapter.setItems(items, activeFilter)
            }
        }
    }

    private fun applyCurrentPipeline() {
        val src = originalBitmap ?: return
        renderJob?.cancel()

        renderJob = lifecycleScope.launch(Dispatchers.Default) {
            val formattedDate = SimpleDateFormat("''yy MM dd", Locale.US).format(Date())

            val builder = PhotoFilter.builder(src)
                .filter(activeFilter, filterIntensity)
                .brightness(brightness)
                .contrast(contrast)
                .warmth(warmth)

            if (includeDateStamp) {
                builder.dateStamp(formattedDate)
            }

            val filtered = builder.build()

            withContext(Dispatchers.Main) {
                binding.splitSliderView.setFilteredBitmap(filtered)
            }
        }
    }

    private fun saveFilteredImageToGallery() {
        val filtered = binding.splitSliderView.getFilteredBitmap() ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val filename = "PhotoFilter_${System.currentTimeMillis()}.jpg"
                var fos: OutputStream? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FilterLibrary")
                    }
                    val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (imageUri != null) {
                        fos = contentResolver.openOutputStream(imageUri)
                    }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val file = File(imagesDir, filename)
                    fos = FileOutputStream(file)
                }

                fos?.use {
                    filtered.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Image saved to Gallery!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shareFilteredImage() {
        val filtered = binding.splitSliderView.getFilteredBitmap() ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cachePath = File(cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "filtered_image.jpg")
                val fos = FileOutputStream(file)
                filtered.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                fos.close()

                val contentUri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "${packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    startActivity(Intent.createChooser(shareIntent, "Share Filtered Photo"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
