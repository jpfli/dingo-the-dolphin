
import java.util.Random;
import femto.mode.HiRes16Color;
import femto.Image;

import images.Bubble0Image;
import images.Bubble1Image;
import images.Bubble2Image;
import images.Bubble3Image;
import images.FloorImage;

class OceanLayer {
    private static final int OCEAN_COLOR = 4, FLOOR_COLOR = 0;
    
    private static final Image[] _bubbles_img = {
        new Bubble2Image(), new Bubble1Image(), new Bubble1Image(), new Bubble3Image(), 
        new Bubble0Image(), new Bubble2Image(), new Bubble3Image(), new Bubble1Image(), 
        new Bubble1Image(), new Bubble1Image(), new Bubble2Image(), new Bubble1Image(), 
        new Bubble3Image(), new Bubble0Image(), new Bubble2Image(), new Bubble2Image(), 
    };
    
    private static final FloorImage _floorimg = new FloorImage();
    
    private Random _rng;

    private int[] _bubbles_xy = new int[32];
    
    public OceanLayer() {
        _rng = new Random();
        _rng.setSeed(31342);
        
        for(int grid_y = 0; grid_y < 4; ++grid_y) {
            for(int grid_x = 0; grid_x < 4; ++grid_x) {
                int idx = 4*grid_y + grid_x;
                _bubbles_xy[2*idx] = 128*grid_x + _rng.nextInt(128);
                _bubbles_xy[2*idx + 1] = 128*grid_y + _rng.nextInt(128);
            }
        }
    }
    
    public void update() {
        for(int grid_y = 0; grid_y < 4; ++grid_y) {
            for(int grid_x = 0; grid_x < 4; ++grid_x) {
                int idx = 4*grid_y + grid_x;
                _bubbles_xy[2*idx] += ((_rng.nextInt(24) - 12 + 8) >> 4);
                _bubbles_xy[2*idx] &= 511;
                _bubbles_xy[2*idx + 1] -= 1;
                _bubbles_xy[2*idx + 1] &= 511;
            }
        }
    }
    
    public void draw(HiRes16Color screen, int x, int y) {
        if(y < 176) {
            screen.fillRect(0, y, 220, 256 - 16, OCEAN_COLOR);
            
            for(int grid_y = 0; grid_y < 4; ++grid_y) {
                for(int grid_x = 0; grid_x < 4; ++grid_x) {
                    int idx = 4*grid_y + grid_x;
                    int img_y = (_bubbles_xy[2*idx + 1] + y + (512 - 176)) & 511;
                    if(img_y >= y && img_y < 176) {
                        int img_x = (_bubbles_xy[2*idx] + x + (512 - 220)) & 511;
                        _bubbles_img[idx].draw(screen, img_x, img_y, false, false, true);
                    }
                }
            }
        }
        
        y += 256 - 16;
        if(y < 176) {
            int tile_x = x > 0 ? (x + 127)%128 - 127 : x%128;
            for(int idx = 0; idx < 8; ++idx) {
                _floorimg.draw(screen, tile_x, y, false, false, true);
                tile_x += 128;
            }
        }
        
        y += 16;
        if(y < 176) {
            screen.fillRect(0, y, 220, 176 - y, FLOOR_COLOR);
        }
    }
}
