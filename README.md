# FilterLibrary
Android filter library
Easy to use android photo filter library.

 Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  Step 2. Add the dependency
  dependencies {
	        compile 'com.github.UltraVisionStudio:FilterLibrary:0.1.0'
	}
  
  or
  
    dependencies {
	        compile 'com.github.hgayan7:FilterLibrary:0.1.0'
	}
  
  CODE:
  
  public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    PhotoFilter photoFilter;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=(ImageView)findViewById(R.id.imageView);
        photoFilter=new PhotoFilter();
        bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.photo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View view) {
                imageView.setImageBitmap(photoFilter.five(getApplicationContext(),bitmap));
            }
        });
    }
}

Use photofilter.x to use a filter,where x ranges from one to sixteen.
