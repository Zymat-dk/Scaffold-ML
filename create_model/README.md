# TFLITE Model creation

## Setup
To create the tflite model, the tools in this folder is needed. Alot of images are also needed. They need to be in `.jpg` format. These images need to be put in a folder named `raw_images`. 

To automatically create the needed folders and distribute the images correctly, use the script `split_img.py`. This creates a folder with 3 subfolders for training, verfication and testing. These folders each has two subfolders - `images` and `annotations`.

## Label images
To label the images with the correct labels, the tool `labelImg` is used. It can be found on [GitHub](https://github.com/heartexlabs/labelImg), and simply cloned with git. It can be installed by running `pip3 install labelimg`. 

When using labelimg, be sure the save format is set to `PascalVOC`. Import the image folder from before (all 3 folders need to be labled, but only one at a time), and set the save dir to the corrosponding annotations folder. 

Be sure to be as precise as possible for the best trained model. 

## Training 
Follow the instructions in the Jupyter notebook. It can be installed on the local machine, but to avoid an annoying setup, Google Colab can also be used. 

## Usage in app
To use the newly trained model, it can be uploaded to the apps object_detection folder: `<ML App>/object_detection/model.tflite`. Simply replace the existing model, with the new one, and complile the app again. 