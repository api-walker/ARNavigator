package de.dhge.ar.arnavigator.ui;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.dhge.ar.arnavigator.util.Arrow;

public class GLRenderer implements GLSurfaceView.Renderer, SensorEventListener {

    private Arrow arrow;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float[] rotationMatrix = new float[16];
    private float[] gravity;
    private float[] geomagnetic;

    float[] position;
    private long prevTimestamp = -1;

    private SensorManager mSensorManager;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        arrow = new Arrow(null, null);

        rotationMatrix = new float [16];
        Matrix.setIdentityM(rotationMatrix, 0);

        // initialise geomagnetic (values may not be null)
        geomagnetic = new float[3];
        geomagnetic[0] = 15;
        geomagnetic[1] = -44;
        geomagnetic[2] = -33;

        position = new float[3];

        mSensorManager = (SensorManager) NavigationActivity.getSystemServiceHelper(Activity.SENSOR_SERVICE);

        Sensor acc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME);

        Sensor mag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME);

        Sensor linAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, linAcc, SensorManager.SENSOR_DELAY_GAME);
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // set camera view
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.translateM(mViewMatrix, 0, position[0], position[1], position[2]-2);
        Matrix.multiplyMM(mViewMatrix, 0, rotationMatrix, 0, mViewMatrix, 0);
        float[] target = new float[]{0,-1,0,1};
        float[] input = target.clone();
        Matrix.multiplyMV(target, 0, mViewMatrix, 0, input, 0);

        // calculate target direction
        float mag = (float)Math.sqrt(target[0]*target[0] + target[1]*target[1] + target[2]*target[2]);
        if(mag != 0) {
            target[0] /= mag;
            target[1] /= mag;
            target[2] /= mag;
        }
        else
        {
            target[0] = 0;
            target[1] = 0;
            target[2] = 0;
        }

        // set arrow position
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, position[0] + target[0]*2, position[1] + target[1]*2, position[2] + target[2]*2);

        // rotate arrow relative to camera
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        arrow.draw(mMVPMatrix);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1f, 20f);
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static int createShader(int vertexShader, int fragmentShader)
    {
        int program = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program);

        return program;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            if (prevTimestamp == -1 || (event.timestamp - prevTimestamp > 2000000684)) { //first run [in a while], ignore sensor and just update timestamp
                prevTimestamp = event.timestamp;
            } else {
                float deltaT = (event.timestamp - prevTimestamp) / (float) 1e9;
                prevTimestamp = event.timestamp;
                float[] a = event.values;
                float[] velocity = new float[3];
                velocity[0] = velocity[0] + deltaT * (rotationMatrix[0] * a[0] + rotationMatrix[1] * a[1] +
                        rotationMatrix[2] * a[2]);
                velocity[1] = velocity[1] + deltaT * (rotationMatrix[4] * a[0] + rotationMatrix[5] * a[1] +
                        rotationMatrix[6] * a[2]);
                velocity[2] = velocity[2] + deltaT * (rotationMatrix[8] * a[0] + rotationMatrix[9] * a[1] +
                        rotationMatrix[10] * a[2]);

                if(velocity[0] < 0.125f && velocity[1] < 0.125f && velocity[2] < 0.125f)
                    return;

                for (int i = 0; i < 3; i++) {
                    position[i] = position[i] + velocity[i];// * deltaT;
                }
            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float accelX = event.values[0];
            float accelY= event.values[1];
            float accelZ = event.values[2];

            final float alpha = (float) 0.8;

            gravity = new float[3];
            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * accelX;
            gravity[1] = alpha * gravity[1] + (1 - alpha) * accelY;
            gravity[2] = alpha * gravity[2] + (1 - alpha) * accelZ;
            mSensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            geomagnetic = event.values.clone();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}