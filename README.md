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
 
		 dependencies 
 			{
	      			  compile 'com.github.UltraVisionStudio:FilterLibrary:0.1.0'
			}
  
  		or
  
   		 dependencies 
		 	{
	 	     		  compile 'com.github.hgayan7:FilterLibrary:0.1.0'
			}
  
  
  	Sample code:
  
   	PhotoFilter photoFilter;
    	photoFilter=new PhotoFilter();
    	......
    	imageView.setImageBitmap(photoFilter.five(getApplicationContext(),bitmap));
  

