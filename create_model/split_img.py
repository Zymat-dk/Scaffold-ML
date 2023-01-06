import os
import pathlib
import shutil
import random

#working_dir = pathlib.Path.home() # the home directory
working_dir = os.getcwd()
src_dir = os.path.join(working_dir, 'raw_images') # ~/raw_images
src_dir = pathlib.Path(src_dir) # convert the path into a Path object
data_dir = os.path.join(working_dir, 'data')


folders = ["training", "validation", "testing"]
dirs = [os.path.join(data_dir, f) for f in folders]
img_dirs = [os.path.join(d, "images") for d in dirs]
annotation_dirs = [os.path.join(d, "annotations") for d in dirs]

pathlib.Path(data_dir).mkdir(exist_ok=True)

for d in dirs + img_dirs + annotation_dirs:
    pathlib.Path(d).mkdir(exist_ok=True)

image_files = list(src_dir.glob("*.jpg"))
num_of_images = len(image_files)
print(f"Num of images: {num_of_images}")

random.shuffle(image_files)

train_split = int(0.8 * num_of_images)
valid_split = int(0.9 * num_of_images)

train_files = image_files[:train_split]
valid_files = image_files[train_split:valid_split]
test_files = image_files[valid_split:]

for src, dest in zip([train_files, valid_files, test_files], img_dirs):
    for f in src:
        shutil.copy(f, dest)