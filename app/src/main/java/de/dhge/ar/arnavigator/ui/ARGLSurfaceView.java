package de.dhge.ar.arnavigator.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;

import de.dhge.ar.arnavigator.navigation.Node;
import de.dhge.ar.arnavigator.navigation.NodeGraph;

class ARGLSurfaceView extends GLSurfaceView {

    private final GLRenderer mRenderer;

    public ARGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new GLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        setRenderer(mRenderer);

        // current hardcoded map
        // each line has one statement either N or Node; C or Connection
        // N ID X Y Name  -- creates a new Node with ID at (X,Y) and the specified name
        // C ID1 ID2 Dist -- creates a new Connection from Node ID1 to Node ID2 (and vice versa) with the speficied distance
        String definition = "Node 1 0 0 Start\n" +
                "Node 2 2 0 S1\n" +
                "Node 3 4 0 S2\n" +
                "Node 4 7 0 Kreuzung\n" +
                "Node 5 7 -1 Treppe\n" +
                "Node 6 7 -3 Weg\n" +
                "Node 7 7 3 AndererWeg\n" +
                "Node 8 6 4 Unten\n" +
                "Node 9 4 6 Labor\n" +
                "C 1 2 2\n" +
                "C 2 3 2\n" +
                "C 3 4 3\n" +
                "C 4 5 1\n" +
                "C 4 6 3\n" +
                "C 4 7 3\n" +
                "C 5 8 6\n" +
                "C 8 9 4";

        NodeGraph ng = new NodeGraph(definition);
        // calculates the path using the map from start to finish (as string or id)
        ArrayList<Node> path = ng.getPath("Start", "Labor");
    }
}