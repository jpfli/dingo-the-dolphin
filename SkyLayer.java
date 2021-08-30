
import femto.Image;
import femto.mode.HiRes16Color;
import femto.input.Button;

import images.HorizonTile0;
import images.HorizonTile1;
import images.HorizonTile2;
import images.HorizonTile3;

class SkyLayer {
    private static final Image _tiles[] = {new HorizonTile0(), new HorizonTile1(), new HorizonTile2(), new HorizonTile3()};
    
    private static final int SKY_COLOR = 1, OCEAN_COLOR = 4;
    
    private static final int TILE_SIZE = 32;
    
    private ubyte _metatile[] = {0, 2, 3, 0, 1, 2, 1, 0, 2, 1, 3, 2, 1, 2, 1, 1, 2, 0, 2, 3, 2, 0, 0, 3, 2, 0, 1, 1, 3, 0, 0, 2};
    private ubyte _map[] = {0, 1, 2, 0, 1, 2, 0, 1};
    
    public SkyLayer() {
        
    }
    
    public void draw(HiRes16Color screen, int x, int y) {
        if(y > 0) {
            // Draw light blue sky
            screen.fillRect(0, 0, 220, y, SKY_COLOR);
        }
        
        if(y > -TILE_SIZE) {
            // Draw clouds and horizon
            int start_col = x > 0 ? (-31 - x)/32 : -x/32;
            int tile_x = x > 0 ? (x + 31)%32 - 31 : x%32;
            for(int col = start_col; col < start_col + 8; ++col) {
                int tile_idx = _metatile[col < 0 ? (31 + (col - 31)%32) : col%32];
                _tiles[tile_idx].draw(screen, tile_x, y, false, false, true);
                tile_x += TILE_SIZE;
            }
        }
    }
}
