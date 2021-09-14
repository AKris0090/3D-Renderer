# If Running in Java

#### Run Main + change String obj to "sphere", "cube", "fox", "gun", "jeep", or "teapot"
#### You know how it goes
#### Press "c" to choose between models or select a file (MUST CHOOSE .OBJ FILE, other files will not work)
#### 2D projection based off of a youtube guide by javidx9, implemented in Java using PApplet
#### Link to his channel here: https://www.youtube.com/channel/UC-yuWVUplUJZvieEligKBkA

# If Running JAR File
#### Download Java 16 (Version is important)
#### Download JAR file
#### Run JAR file with Java 16

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
