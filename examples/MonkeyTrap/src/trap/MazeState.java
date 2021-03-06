/*
 * $Id$
 *
 * Copyright (c) 2013 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package trap;

import trap.game.SensorArea;
import trap.game.Maze;
import trap.game.MonkeyTrapConstants;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.TestFunction;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BaseAppState;
import java.nio.ByteBuffer;
import trap.game.Direction;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class MazeState extends BaseAppState {

    public static final int PLAYER_VISITED = 0x1;
    public static final int PLAYER_VISIBLE = 0x2;

    private Node mazeRoot;
    private Maze maze;

    private int xSize;
    private int ySize;
    private int[][] fogState;
    
    private ColorRGBA[][] fogColors;
    private ColorRGBA[][] lastFogColors;
    private ColorRGBA[][] currentColors;
    private ImageRaster fogOfWar;    
    private float fogMixLevel = 1f;
    
    private ColorRGBA visited = new ColorRGBA(0,0,0,0.75f);
    private ColorRGBA playerVisible = new ColorRGBA(0,0,0,0);

    private ColorRGBA[] stateColors = {
                ColorRGBA.Black,  // no bits
                visited,          // just visited
                playerVisible,    // just visible
                playerVisible     // visible and visited
            };

    private boolean dirty = true;

    public MazeState( Maze maze ) {
        this.maze = maze;
        this.xSize = maze.getWidth();
        this.ySize = maze.getHeight();
        this.fogState = new int[xSize][ySize];
        this.fogColors = new ColorRGBA[xSize][ySize]; 
        this.lastFogColors = new ColorRGBA[xSize][ySize]; 
        this.currentColors = new ColorRGBA[xSize][ySize]; 
    }
    
    public Maze getMaze() {
        return maze;
    }

    public int getVisibility( int x, int y ) {
        return fogState[x][y];
    }
    
    public boolean isVisible( int x, int y ) {
        if( x < 0 || x >= xSize ) {
            return false;
        }
        if( y < 0 || y >= xSize ) {
            return false;
        }
        return (fogState[x][y] & PLAYER_VISIBLE) != 0; 
    }

    public void setVisited( int x, int y ) {
        fogState[x][y] |= PLAYER_VISITED;
    }

    public void clearVisibility( int bits ) {
        int inverted = ~bits;
        for( int i = 0; i < xSize; i++ ) {
            for( int j = 0; j < ySize; j++ ) {
                fogState[i][j] = fogState[i][j] & inverted;               
            } 
        }
        dirty = true; 
    }

    public void setVisibility( SensorArea area, int bits ) {
        for( int i = 0; i < xSize; i++ ) {
            for( int j = 0; j < ySize; j++ ) {
                if( area.isVisible(i, j) ) {
                    fogState[i][j] = fogState[i][j] | bits;
                }               
            } 
        }
        dirty = true;         
    } 

    private void remix() {
        ColorRGBA mix = new ColorRGBA();
        
        for( int i = 0; i < xSize; i++ ) {
            for( int j = 0; j < ySize; j++ ) {
                ColorRGBA color = fogColors[i][j];
                if( fogMixLevel < 1 ) {
                    ColorRGBA last = lastFogColors[i][j];
                    if( last != null ) {
                        mix.interpolate(last, color, fogMixLevel);
                        color = mix;
                    }
                }
                currentColors[i][j] = color.clone();
                fogOfWar.setPixel(i, j, color);  
            }
        }
    }

    protected void refresh() {
        dirty = false;

        ColorRGBA[][] temp = lastFogColors;
        lastFogColors = fogColors;
        fogColors = temp;
        
        for( int i = 0; i < xSize; i++ ) {
            for( int j = 0; j < ySize; j++ ) {
                int value = fogState[i][j];
                fogColors[i][j] = stateColors[value];  
            }
        }
        
        fogMixLevel = 0;        
    }

    protected void generateMazeGeometry() {
        float mazeScale = 2;
                

        GuiGlobals globals = GuiGlobals.getInstance();
        
        Mesh mesh = MeshGenerator.generateMesh(maze, mazeScale, mazeScale);
        Geometry geom = new Geometry("maze", mesh);
        Texture tex = globals.loadTexture("Textures/trap-atlas.png", true, true );
        Material mat = globals.createMaterial(tex, true).getMaterial();
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setFloat("AlphaDiscardThreshold", 0.9f);
        geom.setMaterial(mat);
        geom.move(-mazeScale * 0.5f, 0, -mazeScale * 0.5f);
        geom.setUserData("layer", 0);

        mazeRoot.attachChild(geom);
        
        Mesh aoMesh = MeshGenerator.generateAmbientOcclusion(maze, mazeScale, mazeScale);
        geom = new Geometry("maze", aoMesh);
        tex = globals.loadTexture("Textures/ao-halo-alpha.png", true, true );
        mat = globals.createMaterial(tex, false).getMaterial();
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthFunc(TestFunction.Equal);
        //mat.setFloat("AlphaDiscardThreshold", 0.1f);
        mat.setColor("Color", ColorRGBA.Black);
        geom.setMaterial(mat);
        geom.move(-mazeScale * 0.5f, 0, -mazeScale * 0.5f);
        geom.setQueueBucket(Bucket.Transparent);
        geom.setUserData("layer", 1);

        mazeRoot.attachChild(geom);
 
        // Create the overlay texture we will use for "fog of war"
        int dataSize = maze.getWidth() * maze.getHeight() * 4; 
        ByteBuffer data = BufferUtils.createByteBuffer(dataSize);
        Image img = new Image(Format.ABGR8, maze.getWidth(), maze.getHeight(), data);
        fogOfWar = ImageRaster.create(img);
        tex = new Texture2D(img);
 
        Mesh overlayMesh = MeshGenerator.generateOverlay( maze, mazeScale, mazeScale );
        geom = new Geometry("overlay", overlayMesh);
        mat = globals.createMaterial(tex, false).getMaterial();
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthFunc(TestFunction.Equal);
        geom.setMaterial(mat);
        geom.move(-mazeScale * 0.5f, 0, -mazeScale * 0.5f);
        geom.setQueueBucket(Bucket.Transparent);
        geom.setUserData("layer", 2);
        mazeRoot.attachChild(geom);
    }

    @Override
    protected void initialize( Application app ) {
        mazeRoot = new Node("mazeRoot");        
        generateMazeGeometry();                      
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    public void update( float tpf ) {
        if( dirty ) {
            refresh();
        }
       
        if( fogMixLevel < 1 ) {
            // Mix at the rate we travel... so that it looks
            // the coolest while we are walking.
            float rate = (float)(MonkeyTrapConstants.MONKEY_MOVE_SPEED / 2f);
            fogMixLevel += tpf * rate; //4; //0.1f;
            fogMixLevel = Math.min(fogMixLevel, 1);
            remix();
        }
        
        ModelState models = getState(ModelState.class);
        if( models == null ) {
            return;
        }
         
        for( Spatial s : models.spatials() ) {
            ColorControl cc = s.getControl(ColorControl.class);
            if( cc == null ) {
                continue;
            }
            
            Vector3f loc = s.getLocalTranslation();
            int x = (int)(loc.x * 0.5);
            int y = (int)(loc.z * 0.5);
 
            float visLevel = 0;           
            if( isVisible(x,y) ) {
                visLevel = 1;            
            } else {
                // See if any neighbors are visible
                for( Direction d : Direction.values() ) {
                    int nx = x + d.getXDelta();
                    int ny = y + d.getYDelta();
                    if( isVisible(nx, ny) ) {
                        visLevel += 1;
                    }
                }
                visLevel = visLevel * 0.25f;
            }                       
            cc.setAlpha(visLevel);
        }
    }

    @Override
    protected void enable() {
        Node rootNode = ((SimpleApplication)getApplication()).getRootNode(); 
        rootNode.attachChild(mazeRoot);                                             
    }

    @Override
    protected void disable() {
        mazeRoot.removeFromParent();
    }
}
