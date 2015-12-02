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
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;

import com.airhockey.android.util.LoggerConfig;
import com.airhockey.android.util.ShaderHelper;
import com.airhockey.android.util.TextResourceReader;

public class AirHockeyRenderer implements Renderer {
    private static final String U_COLOR = "u_Color";
    private static final String A_POSITION = "a_Position";    
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer vertexData;
    private final Context context;
    private int program;
    private int uColorLocation;
    private int aPositionLocation;
    private int[] points = {
            //head
            920,520,
            760,720,
            880,480,

            880,480,
            760,720,
            760,520,

            760,520,
            760,720,
            680,560,

            760,720,
            680,560,
            360,480,

            480,200,
            360,480,
            680,560,

            480,200,
            360,480,
            400,160,

            360,480,
            400,160,
            -240,480,
//21
            400,160,
            -240,480,
            400,80,

            -240,480,
            400,80,
            -400,440,

            400,80,
            -400,440,
            320,80,

            -400,440,
            320,80,
            -120,120,

            -400,440,
            -120,120,
            -200,160,

            -400,440,
            -200,160,
            -440,280,
//39
            -200,160,
            -440,280,
            -400,160,

            -360,120,
            -400,160,
            -200,160,

            -320,0,
            -360,120,
            -200,160,

            -240,0,
            -320,0,
            -200,160,

            -200,160,
            -240,0,
            -120,120,

            -160,-40,
            -240,0,
            -120,120,

            -200,-280,
            -160,-40,
            -240,0,

            -160,-240,
            -160,-280,
            -120,-280,

            -200,-280,
            -160,-40,
            -160,-280,

            -320,0,
            -240,0,
            -280,-240,

            -280,-240,
            -320,0,
            -320,-280,

            -280,-240,
            -320,-280,
            -240,-280,


//tail 75
            -520,360,
            -480,320,
            -400,440,

            -560,40,
            -520,360,
            -480,320,

            -480,80,
            -560,40,
            -480,320,

            -520,-80,
            -480,80,
            -560,40,

//front leg 87
            400,160,
            480,200,
            440,-40,

            480,200,
            440,-40,
            480,-240,

            440,-280,
            440,-40,
            480,-240,

            440,-280,
            480,-240,
            520,-280,

            320,80,
            400,80,
            360,-240,

            320,-280,
            360,-240,
            320,80,

            400,-280,
            320,-280,
            360,-240,

            720,680,
            760,720,
            720,760,
//eye 111
            780,640,
            770,670,
            750,650,


    };

    public AirHockeyRenderer() {
        // This constructor shouldn't be called -- only kept for showing
        // evolution of the code in the chapter.
        context = null;
        vertexData = null;
    }

    public AirHockeyRenderer(Context context) {
        this.context = context;
        
        /*
		float[] tableVertices = { 
			0f,  0f, 
			0f, 14f, 
			9f, 14f, 
			9f,  0f 
		};
         */
        /*
		float[] tableVerticesWithTriangles = {
			// Triangle 1
			0f,  0f, 
			9f, 14f,
			0f, 14f,

			// Triangle 2
			0f,  0f, 
			9f,  0f,							
			9f, 14f			
			// Next block for formatting purposes
			9f, 14f,
			, // Comma here for formatting purposes			

			// Line 1
			0f,  7f, 
			9f,  7f,

			// Mallets
			4.5f,  2f, 
			4.5f, 12f
		};
         */
        float[] tableVerticesWithTriangles = calculatepoints();
        
        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }

    private float[] calculatepoints() {
        int length = points.length;
        float floatPoints[] = new float[length];
        for (int i=0;i<length;i++){
            floatPoints[i] = (float)(points[i] / 1480.0);
        }
        return floatPoints;
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        /*
		// Set the background clear color to red. The first component is red,
		// the second is green, the third is blue, and the last component is
		// alpha, which we don't use in this lesson.
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
         */

        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        String vertexShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }

        glUseProgram(program);

        uColorLocation = glGetUniformLocation(program, U_COLOR);
        
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        
        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, 
            false, 0, vertexData);

        glEnableVertexAttribArray(aPositionLocation);
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
        glViewport(0,0, width, height);
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);
        
        // Draw the table.
        glUniform4f(uColorLocation, 0.534f, 0.335f, 0.218f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, points.length/2);

        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 111, 3);

        
        // Draw the center dividing line.
        //glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        //glDrawArrays(GL_LINES, 6, 2);
        
        // Draw the first mallet blue.        
        //glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        //glDrawArrays(GL_POINTS, 8, 1);

        // Draw the second mallet red.
        //glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        //glDrawArrays(GL_POINTS, 9, 1);
    }
}
