package de.dhge.ar.arnavigator.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

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
    }
}