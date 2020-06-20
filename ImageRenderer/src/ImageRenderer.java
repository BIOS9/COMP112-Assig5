// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP112 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP112 - 2018T1, Assignment 5
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import ecs100.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.Color;

/** Renders plain ppm images onto the graphics panel
 *  ppm images are the simplest possible colour image format.
 */

public class ImageRenderer{
    public static final int TOP = 20;   // top edge of the image
    public static final int LEFT = 20;  // left edge of the image
    public static final int PIXEL_SIZE = 2;  
    private BufferedImage buffer; //Double buffer to make animations smoother and minimize loading rolling shutter type effect
    private Graphics bufferGraphics; //Graphics object for the image buffer, avoids calling GetGraphics() many times

    public enum ColorMode //Color modes that are available for the renderComplexAnimatedImage method
    {
        RGB, GREYSCALE, BW
    }

    /** Core:
     * Renders a ppm image file.
     * Asks for the name of the file, then calls renderImageHelper.
     */
    public void renderP3Image(){
        String filePath = UIFileChooser.open(); //Ask user for image file
        try
        {
            File imageFile = new File(filePath);
            CustomScanner sc = new CustomScanner(new Scanner(imageFile));
            if(!sc.next().equalsIgnoreCase("p3")) //Check if first token is P3 header for PBM image files
            {
                UI.println("Invalid image file!");
                return; //Exit method
            }
            renderImageHelper(sc);
        }
        catch (Exception ex)
        {
            UI.println("Error rendering image: " + ex);
        }
    }

    /** Core:
     * Renders a ppm image file.
     * Renders the image at position (LEFT, TOP).
     * Each pixel of the image  is rendered by a square of size PIXEL_SIZE
     * Assumes that
     * - the colour depth is 255,
     * - there is just one image in the file (not "animated"), and
     * - there are no comments in the file.
     * The first four tokens are "P3", number of columns, number of rows, 255
     * The remaining tokens are the pixel values (red, green, blue for each pixel)
     */
    public void renderImageHelper(CustomScanner sc)
    {
        int width = sc.nextInt(); //Get width of image
        int height = sc.nextInt(); //Get height of image
        int cDepth = sc.nextInt(); //Get colour depth of image

        buffer = new BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB); //Initializes the double buffer with a height and width of two for 200% scaling

        for(int i = 0; i < height; i++) { //Iterate over rows of pixels
            for(int j = 0; j < width; j++) { //Draw each row of pixels by drawing the pixel in each column.
                int p = pixel(sc, cDepth); //Get next pixel from file
                drawPixel(j, i, p); //Draw pixel at specified co-ordinates with the specified color
            }
        }

        flushBuffer(); //Render the image to the UI/flush the image buffer

        if(sc.hasNext() && sc.next().equalsIgnoreCase("p3")) { //If there is another P3 token indicating another image after the current image
            UI.sleep(100); //200ms delay to slow down the animation
            renderImageHelper(sc); //Render the next image using the same scanner
        }
    }

    /**
     * Draw pixel on the image buffer.
     * @param x X co-ordinate of pixel to draw
     * @param y Y co-ordinate of pixel to draw
     * @param color RGB color of pixel to draw
     */
    private void drawPixel(int x, int y, int color)
    {
        if(bufferGraphics == null) //If the buffer graphics object is not set
            bufferGraphics = buffer.getGraphics();
        bufferGraphics.setColor(new Color(color)); //Set color of pixel
        bufferGraphics.fillRect(
                (x * PIXEL_SIZE), //X value of pixel, scale by pixel size
                (y * PIXEL_SIZE), //Y value of pixel, scale by pixel size
                PIXEL_SIZE, PIXEL_SIZE); //Pixel size width x height*/
    }

    /**
     * Draw pixel on the image buffer.
     * @param x X co-ordinate of pixel to draw
     * @param y Y co-ordinate of pixel to draw
     * @param color greyscale color of pixel to draw
     */
    private void drawPixel(int x, int y, byte color)
    {
        //Convert grayscale color to RGB
        int newColor = color << 16; //Shift color 2 bytes to the left
        newColor += color << 8; //Shift another copy of color 1 byte to the left
        newColor += color; //Add another copy of color and dont shift
        drawPixel(x, y, newColor); //Call RGB version with the converted colour
    }

    /**
     * Method to render buffered image then clear the buffer
     */
    private void flushBuffer()
    {
        bufferGraphics.dispose(); //Free graphics object reduce load on GC
        UI.drawImage(buffer, LEFT, TOP); //Draw the image with the TOP and LEFT offsets
        bufferGraphics = null; //Set to null so that the drawPixel method knows to get the new graphics object from a new image.
    }

    /**
     * Helper method to get one full coloured RGB pixel from the scanner
     * @param sc Scanner to read from
     * @param depth Colour depth of image
     * @return Colour of pixel as an integer
     */
    private int pixel(CustomScanner sc, int depth)
    {
        //Red, Green and Blue are all scaled by 255/colour depth to ensure smaller colour depth images come out correct
        //new 4 byte integer, starts as 0x00000000
        int color = (sc.nextInt() * 255 / depth) << 16; //Shift nextInt 16 bits (2 bytes) into the int, if nextInt was FF, int becomes 0x00FF0000
        color += (sc.nextInt() * 255 / depth) << 8; //Shift nextInt 8 bits (1 byte) into the int, if nextInt was BB, int becomes 0x00FFBB00
        color += (sc.nextInt() * 255 / depth); //This byte doesn't get shifted, if nextInt was 22, int becomes 0x00FFBB22
        //There is still one byte of space left in the integer. This could be used for an alpha/opacity value of the pixel.
        return color;
    }

    /**
     * Helper method to get one grayscale pixel from the scanner
     * @param sc Scanner to read from
     * @param depth Colour depth of image
     * @return Colour of pixel as an integer
     */
    private byte greyPixel(CustomScanner sc, int depth)
    {
        byte color = 0; //Color from 0 - 255 because tis greyscale
        //color = (byte)(sc.nextInt() * 255 / depth); //set the color to the scaled value
        color = (byte)((255 - (sc.nextInt() * 255)) / depth);
        return color;
    }

    /** Core
     * Renders a ppm image file which may be animated (multiple images in the file)
     * Asks for the name of the file, then renders the image at position (LEFT, TOP).
     * Each pixel of the image  is rendered by a square of size PIXEL_SIZE
     * Renders each image in the file in turn with 200 mSec delay.
     * Repeats the sequence 3 times.
     */
    public void renderAnimatedImage(){
        String filePath = UIFileChooser.open(); //Ask user for image file
        try
        {
            File imageFile = new File(filePath);
            CustomScanner sc = new CustomScanner(new Scanner(imageFile)); //Create new custom scanner that ignores comments. Initialize inner stream of image file
            if(!sc.next().equalsIgnoreCase("p3")) //Check if first token is P3 header for PBM image files
            {
                UI.println("Invalid image file!");
                return; //Exit method
            }
            renderImageHelper(sc);
        }
        catch (Exception ex)
        {
            UI.println("Error rendering image: " + ex);
        }
    }

    /** Completion
     * Renders a ppm image file which may be animated (multiple images in the file)
     * Asks for the name of the file, then renders the image at position (LEFT, TOP).
     * Each pixel of the image  is rendered by a square of size PIXEL_SIZE
     * Renders each image in the file in turn with 200 mSec delay.
     * Repeats the sequence 3 times.
     * Ignores comments (starting with # and occuring after the 1st, 2nd, or 3rd token) 
     * The colour depth (max colour value) may be different from 255 (scales the
     * colour values appropriately)
     */
    public void renderComplexAnimatedImage(){
        String filePath = UIFileChooser.open(); //Ask user for image file
        try
        {
            File imageFile = new File(filePath);
            CustomScanner sc = new CustomScanner(new Scanner(imageFile)); //Create new custom scanner that ignores comments. Initialize inner stream of image file
            String header = sc.next(); //Header stored in variable so it can be checked twice without advancing the scanner position
            if(header.equalsIgnoreCase("p3")) //Check if first token is P3 header for PBM image files
                renderComplexImageHelper(sc, ColorMode.RGB); //Render the image as a coloured PGM
            else if(header.equalsIgnoreCase("p2")) //Check if first token is P2 header for plain PBM image files
                renderComplexImageHelper(sc, ColorMode.GREYSCALE); //Render the image as a plain PGM
            else if(header.equalsIgnoreCase("p1")) //Check if first token is P1 header for black and white plain PBM image files
                renderComplexImageHelper(sc, ColorMode.BW); //Render the image as a black and white plain PGM
            else
                UI.println("Invalid image file!");
        }
        catch (Exception ex)
        {
            UI.println("Error rendering image: " + ex);
        }
    }

    public void renderComplexImageHelper(CustomScanner sc, ColorMode mode){
        int width = sc.nextInt(); //Get width of image
        int height = sc.nextInt(); //Get height of image
        int cDepth = (mode != ColorMode.BW) ? sc.nextInt() : 0; //Get colour depth of image. Ignore if the image mode is black and white

        if(mode == ColorMode.GREYSCALE || mode == ColorMode.BW)
            buffer = new BufferedImage(width * 2, height * 2, BufferedImage.TYPE_BYTE_GRAY); //Initializes the double buffer with a height and width of two for 200% scaling
        else
            buffer = new BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB); //Initializes the double buffer with a height and width of two for 200% scaling

        for(int i = 0; i < height; i++) { //Iterate over rows of pixels
            for(int j = 0; j < width; j++) { //Draw each row of pixels by drawing the pixel in each column.
                if(mode == ColorMode.GREYSCALE || mode == ColorMode.BW)
                {
                    byte p = greyPixel(sc, (mode == ColorMode.GREYSCALE) ? cDepth : 1); //Get next grayscale pixel from file. Use 1 as colour depth if the image is black and white
                    drawPixel(j, i, p); //Draw pixel at specified co-ordinates with the specified color
                }
                else
                {
                    int p = pixel(sc, cDepth); //Get next pixel from file.
                    drawPixel(j, i, p); //Draw pixel at specified co-ordinates with the specified color
                }
            }
        }

        flushBuffer(); //Render the image to the UI/flush the image buffer

        if(sc.hasNext() && sc.next().equalsIgnoreCase("p3")) { //If there is another P3 token indicating another image after the current image
            UI.sleep(100); //200ms delay to slow down the animation
            renderComplexImageHelper(sc, mode); //Render the next image using the same scanner
        }
    }

    /**
     * Compress a PGM file using run-length compression
     */
    public void compressBWPBM()
    {
        String filePath = UIFileChooser.open(); //Ask user for image file
        try
        {
            String outputPath = removeExtension(filePath) + ".compressedpbm";
            File imageFile = new File(filePath);
            File outputImageFile = new File(outputPath);
            CustomScanner sc = new CustomScanner(new Scanner(imageFile)); //Create new custom scanner that ignores comments. Initialize inner stream of image file
            PrintWriter pw = new PrintWriter(outputImageFile); //Create new print writer to write new compressed file
            String header = sc.next(); //Get the image type header
            if(!header.equalsIgnoreCase("p1"))
            {
                UI.println("Invalid black and white PPM!");
                return;
            }
            pw.write("C" + header + " "); //Write header, add C so the program can tell its a compressed image
            pw.write(sc.next() + " "); //Write width
            pw.write(sc.next() + " "); //Write height

            int length = 1; //Start length at 1 so it is a count rather than an index
            int last = sc.nextInt(); //Initialize first color value for the while loop
            if(last != 0) //Ensure file compressed file starts with 0 for the image data
                pw.write("0 "); //Set first number to 0 to indicate the start is a 1

            while(sc.hasNext())
            {
                int next = sc.nextInt();
                if(next == last) //If the current number is the same, increase the length
                    ++length;
                else //Different number
                {
                    pw.write(length + " "); //Write length to file
                    last = next;
                    length = 1;
                }
            }
            pw.write(length + " "); //Write final length value to file once loop exits
            pw.close();
            UI.println("Image has been compressed!");
            UI.println("Saved to: " + outputPath);
        }
        catch (Exception ex)
        {
            UI.println("Error rendering image: " + ex);
        }
    }

    /**
     * Removes extension from path string
     * @param path path with extension
     * @return path without extension
     */
    public String removeExtension(String path)
    {
        return path.substring(0, path.indexOf('.'));
    }

    /**
     * Decompress a run-length compressed PGM file
     */
    public void decompressBWPBM()
    {
        String filePath = UIFileChooser.open(); //Ask user for image file
        try
        {
            String outputPath = removeExtension(filePath) + ".decompressed.pbm";
            File imageFile = new File(filePath);
            File outputImageFile = new File(outputPath);
            CustomScanner sc = new CustomScanner(new Scanner(imageFile)); //Create new custom scanner that ignores comments. Initialize inner stream of image file
            PrintWriter pw = new PrintWriter(outputImageFile); //Create new PrintWriter for writing the new decompressed image
            String header = sc.next(); //Get file type header
            if(!header.equalsIgnoreCase("cp1")) //Check that image is a compressed image
            {
                UI.println("Invalid compressed black and white PPM!");
                return;
            }
            pw.write("P1\n"); //Write header
            pw.write(sc.next() + " "); //Write width
            pw.write(sc.next() + "\n"); //Write height


            /*
            Since there are only two values in the black and white image, the output of compression will alternate between between black and white e.g. "0 <length> 1 <length>"
            I removed the 1's and 0's because they are predictable so therefore are a detriment to the compression. The code below alternates between black and white with every value.
            My program originally assumed that the image started with white, but that meant the image would be inverted if it started with black. If the image starts with black, 0 will be
            prepended to the image so the second pixel is the black.
             */
            boolean next = false; //Toggle for black or white
            int length = sc.nextInt(); //Get first pixel value
            if(length == 0) //if the image data actually begins with 1 (indicated by a 0), flip the next state to 1
            {
                next = true;
                length = sc.nextInt();
            }
            int lineLength = 0; //Length of current line in the image. Keeps text in a rectangular block instead of all on one line

            while(true)
            {
                for(int i = 0; i < length; ++i) //Loop for length of black or white sequence.
                {
                    if(next) //If white
                        pw.write("1 ");
                    else //If black
                        pw.write("0 ");
                    ++lineLength;
                    if(lineLength >= 39) //keep line length 60 or below to make the file easily human readable for troubleshooting. I chose 39 because that was what was used in the provided images
                    {
                        pw.write("\n"); //Add newline
                        lineLength = 0; //Reset line length
                    }
                }
                if(!sc.hasNext()) break; //Break here in the loop to ensure the code above runs before final length value
                length = sc.nextInt();
                next = !next; //Flip next mode
            }
            pw.close();
            UI.println("Image has been decompressed!");
            UI.println("Saved to: " + outputPath);
            try {
                renderComplexImageHelper(new CustomScanner(new Scanner(new File(outputPath))), ColorMode.BW); //Render the image after decompression finishes
            }catch (Exception ex)
            {
                UI.println("Failed to render image: " + ex);
            }
        }
        catch (Exception ex)
        {
            UI.println("Error rendering image: " + ex);
        }
    }

}
