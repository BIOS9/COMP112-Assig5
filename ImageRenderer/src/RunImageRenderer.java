// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP112 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP112 - 2018T1, Assignment 5
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import ecs100.*;

public class RunImageRenderer{

    public static void main(String[] args){
        ImageRenderer ir = new ImageRenderer();
        UI.initialise();
        UI.addButton("Clear", UI::clearGraphics );
        UI.addButton("Core: Render P3 image", ir::renderP3Image );
        UI.addButton("Core: Render Animated image", ir::renderAnimatedImage );
        UI.addButton("Completion: Render Complex Animated image", ir::renderComplexAnimatedImage );
        UI.addButton("Challenge: Compress BW image", ir::compressBWPBM );
        UI.addButton("Challenge: Decompress BW image", ir::decompressBWPBM );
        UI.addButton("Quit", UI::quit );
        UI.setWindowSize(850, 700);
        UI.setDivider(0.0);
    }
}
