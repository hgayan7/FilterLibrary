[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/hgayan7/FilterLibrary/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/Naereen/StrapDown.js.svg)](https://github.com/hgayan7/FilterLibrary/graphs/contributors/)
[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)


# FilterLibrary                        

Easy to use android image filter library.

[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/0)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/0)[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/1)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/1)[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/2)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/2)[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/3)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/3)[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/4)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/4)[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/5)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/5)[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/6)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/6)[![](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/images/7)](https://sourcerer.io/fame/hgayan7/hgayan7/FilterLibrary/links/7)

### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
``` java

allprojects 
{
	repositories 
	{
		maven { url 'https://jitpack.io' }
	}
}
```

### Step 2. Add the dependency
 ``` java	
 
dependencies 
{
	compile 'com.github.hgayan7:FilterLibrary:0.1.0'
}
 ``` 
 
 ### Sample code:
  ``` java
  
  ImageView myImageView1,myImageView2;
  Bitmap myBitmap1,myBItmap2;
  
  PhotoFilter photoFilter;
  
  myImageView1 = findViewById(R.id.imageView1);
  myBitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.myImage1);
  
  myImageView2 = findViewById(R.id.imageView2);
  myBitmap2 = BitmapFactory.decodeResource(getResources(),R.drawable.myImage2);
  
  photoFilter = new PhotoFilter();
  
  // using filter 'one'
  myImageView1.setImageBitmap(photoFilter.one(getApplicationContext(),myBitmap1));
  
  // using filter 'sixteen'
  myImageView2.setImageBitmap(photoFilter.sixteen(getApplicationContext(),myBitmap2));
  
  ```
## Screenshots :

### Original image :


<p align="center">
  <img width="350" height="250" src="https://user-images.githubusercontent.com/29502161/50734591-55f82280-11c7-11e9-8fb9-0e1c544c73d8.jpeg">
</p>


### Images using filters :

<p align="center" >
  <img width="350" height="250" src="https://user-images.githubusercontent.com/29502161/50734598-6c05e300-11c7-11e9-82a9-321b043c38e4.jpeg" hspace="20">
  <img width="350" height="250" src="https://user-images.githubusercontent.com/29502161/50734599-6dcfa680-11c7-11e9-9b0d-b957a567610f.jpeg" hspace="20">	
</p>

<p align="center">
  <img width="350" height="250" src="https://user-images.githubusercontent.com/29502161/50734600-6f00d380-11c7-11e9-94cc-f8c8cfef3533.jpeg" hspace="20">
  <img width="350" height="250" src="https://user-images.githubusercontent.com/29502161/50734601-70320080-11c7-11e9-995a-db77b66a1012.jpeg" hspace="20">	
</p>

<p align="center">
  <img width="350" height="250" src="https://user-images.githubusercontent.com/29502161/50734604-732cf100-11c7-11e9-9fbd-1e55ddb5631e.jpeg" hspace="20">
  <img width="350" height="250" src="https://user-images.githubusercontent.com/29502161/50734603-71632d80-11c7-11e9-8cc5-c13b080a06dc.jpeg" hspace="20">	
</p>


