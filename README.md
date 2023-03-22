# Quick Start Guide
#### https://youtu.be/teK-erm_5Hg

# If Running JAR File
#### Download Java 16 (Version is important)
#### Download project
#### Unzip project
#### Run JAR file with Java 16 (3D_Renderer_jar/3D Renderer)

# 3D Renderer Project Description

  This project is involves projection of 3D coordinates to 2D space, with math based off of a youtube guide by javidx9, implemented in Java using PApplet
#### Link to his channel here: https://www.youtube.com/channel/UC-yuWVUplUJZvieEligKBkA

  Starting off, the coordinates in 3D space are imported from the .obj file, where the single vertices are marked with a "v" followed by a sequence of numbers representing their position in 3D space. This is called by the class ObjectLoader. The .obj files also gives the faces of an object, denoted by an "f" at the start of a line. The set of numbers following an "f" denotation mark indexes of the vertices that make up the face. By arranging the vertices into an ArrayList, we can easily get faces by calling the index stored within the List. Also, within the setup of the application, I also set the camera's position in the 3D world as the point the scene is viewed from.

  Next, I load a projection matrix from a couple of predefined constants. The FOV (default - 270 deg), the aspect ratio of the viewing plane (default - 16:9), an FOV multiplier, which is arctan of the FOV, in radians, divided by two, and a viewing distance, or Z, multiplier (default - 1.001001). Using my MatrixMath class, I generate the projection matrix with these values. A projection matrix is a 4x4 matrix that, when multiplied by a 3 dimensional vector, can return another vector in 2 dimensions corresponding to a predetermined viewing plane.

  Now, the actual drawing of the preloaded triangles onto the 2D screen. to start off, I initiate rotation matrices using a predetermined angle so that rotation of the model is only activated if the user wants to. I multiply the triangles by these rotation matrices (167). Then, I translate the triangles in 3D space so that they appear in the middle of the screen with translation matrices (249). Next, I calculate the normals of each triangle by using the cross product between two of the 3 points that make up the triangle and normalizing the result. Using these normals, I calculate which triangles are able to be seen by the camera using the dot product between the normals and the camera vector. I recalculate the normals for these, and then use the dot product between the normals of the triangles and the 3D coordinates of a pre-determined light source to calculate the color with which the triangles should be displayed with. I then sort the list of triangles in a reverse order so that the furthest triangles are displayed first, and then the closest ones, so that there is no overlapping triangles and see-through models. Finally, I project these triangles into a 2D plane by multiplying them with the preloaded projection matrix, and finally draw the models onto screen.

## Some features I added are:
#### -Turning on and off an axis visualization
#### -Turning on and off the wireframe of the model
#### -Turning on and off the model faces
#### -Moving the lightsource to different locations
#### -Free rotation
#### -Zooming in and out
#### -A bounding box
#### -Mouse drag to rotate the model on the x or y axis
#### -Right mouse drag to pan the model on the x or y axis

# If Running in IDE

#### Run Main + change String obj to "sphere", "cube", "fox", "gun", "jeep", or "teapot"
#### Press "c" to choose between models or select a file (MUST CHOOSE .OBJ FILE, other files will not work)

# Updates

## 6/20/2022
#### - Added vsync embedded in PJOGL
#### - Bug fixes: reloading the model on FOV changes (fixed)
#### - Added a loading bar for larger files
#### - More optimization incoming

## 6/19/2022
#### - Press 1 for surface normals
#### - Plan to implement small normals for each triangle
#### - Will probably update occlusion culling with a more efficient algorithm, instead of bruteforcing

## 6/9/2022
#### - added optimization, less creation of multiple arraylists. One arraylist of triangles, passed through all methods.
#### - added 2 more preloaded models, duck and deer
##### - ideas:
###### - add color profiles again (deleted due to accident)
###### - add occlusion culling to further optimize by reducing number of triangles drawn. Possible threshold on when to include culling or not.

## 9/13/2021
#### - added options to select preloaded models or select file to load
#### - fixed bug with closing windows
#### - added JAR file for easy run

## 5/4/2021
#### - control light with i - up, j - left, k - down, l - right
#### - option to have the axis visualization, ctrl + f and axis visualization, comment it out
#### - to cancel rotation, change angle += 0.01 to += 0.0 or comment it out, both work
#### - option to show wireframe, comment out triangle() in drawtriangles method and uncomment the wireframe lines
#### - keep aspect ratio 1:1 if changing xSize or ySize
#### - dont ask me why camera is at 0 0 -100000000 it works
#### - Fixed Issue of X-Ray Vision
#### - will update GUI and optimization

## 5/9/2021
#### - can now drag to control x rotation and y rotation or choose to free rotate
#### - settings window will show what keys do when pressed
#### - s to open settings window if accidentally closed
#### - optimized a bit, no double calculations as far as I can tell

## 5/10/2021
#### - added mouse wheel scroll zooming, does not work with a trackpad as far as I have tested
#### - 2 more models - gun and jeep
