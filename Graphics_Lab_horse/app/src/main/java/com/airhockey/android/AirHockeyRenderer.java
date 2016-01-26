/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.airhockey.android;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.airhockey.android.util.LoggerConfig;
import com.airhockey.android.util.MatrixHelper;
import com.airhockey.android.util.ShaderHelper;
import com.airhockey.android.util.TextResourceReader;

public class AirHockeyRenderer implements Renderer {

    private static final String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;
    private final float[] modelMatrix = new float[16];

    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private final FloatBuffer vertexData;
    private final Context context;

    private int program;
    private int aPositionLocation;
    private int aColorLocation;

    public AirHockeyRenderer(Context context) {
        this.context = context;

        float[] tableVerticesWithTriangles = {
                //body
                0.621622f,0.351351f,0.584991f,0.479858f,0.350281f,
                0.513514f,0.486486f,0.174103f,0.858917f,0.71048f,
                0.594595f,0.324324f,0.0914001f,0.364441f,0.147308f,

                0.594595f,0.324324f,0.11908f,0.00466919f,0.00891113f,
                0.513514f,0.486486f,0.601746f,0.607147f,0.166229f,
                0.513514f,0.351351f,0.0570374f,0.607666f,0.783295f,

                0.513514f,0.351351f,0.875946f,0.726654f,0.955872f,
                0.513514f,0.486486f,0.462067f,0.235321f,0.862213f,
                0.459459f,0.378378f,0.996765f,0.999664f,0.611481f,

                0.513514f,0.486486f,0.840118f,0.0237427f,0.375854f,
                0.459459f,0.378378f,0.00878906f,0.918762f,0.275879f,
                0.243243f,0.324324f,0.837585f,0.726471f,0.484924f,

                0.324324f,0.135135f,0.457947f,0.949127f,0.744415f,
                0.243243f,0.324324f,0.734985f,0.608948f,0.572388f,
                0.459459f,0.378378f,0.42514f,0.802856f,0.51709f,

                0.324324f,0.135135f,0.168976f,0.657288f,0.491882f,
                0.243243f,0.324324f,0.147491f,0.949554f,0.141571f,
                0.27027f,0.108108f,0.426544f,0.0703735f,0.966583f,

                0.243243f,0.324324f,0.821655f,0.582031f,0.191345f,
                0.27027f,0.108108f,0.155548f,0.503906f,0.731995f,
                -0.162162f,0.324324f,0.68222f,0.755829f,0.721893f,

                0.27027f,0.108108f,0.834656f,0.0350952f,0.516998f,
                -0.162162f,0.324324f,0.94931f,0.921356f,0.54953f,
                0.27027f,0.0540541f,0.846954f,0.316864f,0.456085f,

                -0.162162f,0.324324f,0.739166f,0.567261f,0.195984f,
                0.27027f,0.0540541f,0.500885f,0.890137f,0.0274658f,
                -0.27027f,0.297297f,0.531311f,0.194061f,0.843018f,

                0.27027f,0.0540541f,0.842133f,0.123322f,0.109924f,
                -0.27027f,0.297297f,0.286072f,0.336304f,0.140259f,
                0.216216f,0.0540541f,0.60022f,0.747192f,0.252716f,

                -0.27027f,0.297297f,0.806213f,0.8526f,0.210571f,
                0.216216f,0.0540541f,0.11377f,0.454498f,0.752197f,
                -0.0810811f,0.0810811f,0.436707f,0.201935f,0.696198f,

                -0.27027f,0.297297f,0.57785f,0.532562f,0.628662f,
                -0.0810811f,0.0810811f,0.69574f,0.924774f,0.189941f,
                -0.135135f,0.108108f,0.457428f,0.997986f,0.0975037f,

                -0.27027f,0.297297f,0.931488f,0.0484314f,0.894592f,
                -0.135135f,0.108108f,0.410706f,0.201965f,0.628052f,
                -0.297297f,0.189189f,0.597809f,0.634705f,0.854767f,

                -0.135135f,0.108108f,0.565735f,0.375122f,0.184265f,
                -0.297297f,0.189189f,0.242859f,0.188934f,0.604706f,
                -0.27027f,0.108108f,0.494446f,0.0803833f,0.740723f,

                -0.243243f,0.0810811f,0.804504f,0.149109f,0.576019f,
                -0.27027f,0.108108f,0.727661f,0.0432129f,0.667755f,
                -0.135135f,0.108108f,0.305817f,0.17392f,0.108551f,

                -0.216216f,0f,0.154877f,0.326904f,0.0793457f,
                -0.243243f,0.0810811f,0.545074f,0.448242f,0.408966f,
                -0.135135f,0.108108f,0.152649f,0.323029f,0.737976f,

                -0.162162f,0f,0.873322f,0.725006f,0.300049f,
                -0.216216f,0f,0.784943f,0.524567f,0.609619f,
                -0.135135f,0.108108f,0.653839f,0.322113f,0.104797f,

                -0.135135f,0.108108f,0.919952f,0.551147f,0.662781f,
                -0.162162f,0f,0.496796f,0.793335f,0.509247f,
                -0.0810811f,0.0810811f,0.606262f,0.395172f,0.00588989f,

                -0.108108f,-0.027027f,0.86322f,0.491486f,0.747314f,
                -0.162162f,0f,0.552795f,0.357086f,0.955688f,
                -0.0810811f,0.0810811f,0.131622f,0.743256f,0.951691f,

                -0.135135f,-0.189189f,0.0559082f,0.639191f,0.131622f,
                -0.108108f,-0.027027f,0.721619f,0.853943f,0.014679f,
                -0.162162f,0f,0.21756f,0.0659485f,0.168915f,

                -0.108108f,-0.162162f,0.367554f,0.66098f,0.802368f,
                -0.108108f,-0.189189f,0.798157f,0.900574f,0.144806f,
                -0.0810811f,-0.189189f,0.136536f,0.855164f,0.0661621f,

                -0.135135f,-0.189189f,0.548035f,0.225555f,0.31134f,
                -0.108108f,-0.027027f,0.284241f,0.788086f,0.895203f,
                -0.108108f,-0.189189f,0.361115f,0.856628f,0.228485f,

                -0.216216f,0f,0.542389f,0.984802f,0.0538025f,
                -0.162162f,0f,0.0946655f,0.258789f,0.89151f,
                -0.189189f,-0.162162f,0.93161f,0.0801086f,0.0470886f,

                -0.189189f,-0.162162f,0.39859f,0.43277f,0.946136f,
                -0.216216f,0f,0.693512f,0.397675f,0.259155f,
                -0.216216f,-0.189189f,0.398682f,0.241089f,0.585541f,

                -0.189189f,-0.162162f,0.435486f,0.890198f,0.00717163f,
                -0.216216f,-0.189189f,0.57666f,0.142426f,0.222321f,
                -0.162162f,-0.189189f,0.0822449f,0.659912f,0.855072f,

                -0.351351f,0.243243f,0.691467f,0.802673f,0.530121f,
                -0.324324f,0.216216f,0.727875f,0.77771f,0.0310669f,
                -0.27027f,0.297297f,0.0854492f,0.551971f,0.947876f,

                -0.378378f,0.027027f,0.98175f,0.619965f,0.292236f,
                -0.351351f,0.243243f,0.218628f,0.155914f,0.24054f,
                -0.324324f,0.216216f,0.902618f,0.441711f,0.0801086f,

                -0.324324f,0.0540541f,0.775848f,0.870361f,0.210632f,
                -0.378378f,0.027027f,0.114044f,0.404694f,0.311127f,
                -0.324324f,0.216216f,0.18927f,0.247681f,0.153503f,

                -0.351351f,-0.0540541f,0.195648f,0.779633f,0.645538f,
                -0.324324f,0.0540541f,0.921265f,0.664154f,0.150757f,
                -0.378378f,0.027027f,0.942993f,0.540558f,0.578552f,

                0.27027f,0.108108f,0.35025f,0.0366211f,0.795227f,
                0.324324f,0.135135f,0.590729f,0.0709229f,0.197662f,
                0.297297f,-0.027027f,0.604279f,0.697327f,0.441284f,

                0.324324f,0.135135f,0.592194f,0.199585f,0.949432f,
                0.297297f,-0.027027f,0.185303f,0.895691f,0.574432f,
                0.324324f,-0.162162f,0.0496216f,0.285553f,0.260193f,

                0.297297f,-0.189189f,0.728424f,0.896027f,0.393555f,
                0.297297f,-0.027027f,0.388092f,0.570557f,0.353546f,
                0.324324f,-0.162162f,0.73941f,0.139313f,0.200165f,

                0.297297f,-0.189189f,0.367828f,0.517639f,0.109375f,
                0.324324f,-0.162162f,0.654816f,0.438507f,0.687622f,
                0.351351f,-0.189189f,0.0278015f,0.355072f,0.30722f,

                0.216216f,0.0540541f,0.0678406f,0.67572f,0.724915f,
                0.27027f,0.0540541f,0.654877f,0.543976f,0.0982971f,
                0.243243f,-0.162162f,0.46402f,0.96994f,0.727844f,

                0.216216f,-0.189189f,0.372101f,0.388611f,0.0499573f,
                0.243243f,-0.162162f,0.310547f,0.784454f,0.212463f,
                0.216216f,0.0540541f,0.43985f,0.507843f,0.605927f,

                0.27027f,-0.189189f,0.842621f,0.942291f,0.623291f,
                0.216216f,-0.189189f,0.863525f,0.386749f,0.306396f,
                0.243243f,-0.162162f,0.391174f,0.0186157f,0.0310364f,

                0.486486f,0.459459f,0.401886f,0.727264f,0.611572f,
                0.513514f,0.486486f,0.595001f,0.07901f,0.793152f,
                0.486486f,0.513514f,0.569183f,0.81604f,0.672729f,

                0.527027f,0.432432f,0.341553f,0.232086f,0.770996f,
                0.52027f,0.452703f,0.725555f,0.628906f,0.893921f,
                0.506757f,0.439189f,0.78006f,0.965179f,0.197357f,

                // Triangle Fan body
                0.351f,0.324f, 0.2f, 0.2f, 0.2f,
                0.2432f,0.324f,  1.0f, 1.0f, 0.0f,
                0.324f,0.135f,  1.0f, 0.0f, 1.0f,
                0.459f,0.378f,  0.0f, 1.0f, 1.0f,
                0.5235f,0.486f,   0.0f, 0.0f, 1.0f,
                0.2432f,0.324f,  1.0f, 0.0f, 0.0f,


                // Triangle Fan head
                0.511f,0.406f, 0.2f, 0.2f, 0.2f,
                0.459f,0.378f,  1.0f, 1.0f, 0.0f,
                0.5135f,0.351f,  1.0f, 0.0f, 1.0f,
                0.5945f,0.324f,  0.0f, 1.0f, 1.0f,
                0.621f,0.351f,   0.0f, 0.0f, 1.0f,
                0.5135f,0.486f,  1.0f, 0.0f, 0.0f,
                0.459f,0.378f,  1.0f, 1.0f, 0.0f,






        };

        vertexData = ByteBuffer
                .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1f, 0.0f);

        String vertexShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper
                .compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }

        glUseProgram(program);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);

        glEnableVertexAttribArray(aPositionLocation);

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_COLOR_LOCATION.
        vertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);

        glEnableVertexAttribArray(aColorLocation);
    }

    /**
     * onSurfaceChanged is called whenever the surface has changed. This is
     * called at least once when the surface is initialized. Keep in mind that
     * Android normally restarts an Activity on rotation, and in that case, the
     * renderer will be destroyed and a new one created.
     *
     * @param width
     *            The new width, in pixels.
     * @param height
     *            The new height, in pixels.
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
//        glViewport(0, 0, width, height);

        /*
        //orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
            // Landscape
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            // Portrait or square
            orthoM(projectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
        }
        */
        MatrixHelper.perspectiveM(projectionMatrix, 60, (float) width
                / (float) height, 1f, 10f);

        setIdentityM(modelMatrix, 0);

        translateM(modelMatrix, 0, 0f, 0f, -2f);
        rotateM(modelMatrix, 0, 00f, 0f, 0f, 1f);

        final float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {

        glClear(GL_COLOR_BUFFER_BIT);
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);


        glDrawArrays(GL_TRIANGLES, 0, 114);

        //Drawing  FAN

        glDrawArrays(GL_TRIANGLE_FAN, 115, 6);
        glDrawArrays(GL_TRIANGLE_FAN, 121, 7);


    }
}