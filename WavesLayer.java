
import femto.mode.HiRes16Color;

import images.WaveImage;

class WavesLayer {
    private static final int IMAGE_W = 32;
    private static final int IMAGE_H = 8;
    
    private static final WaveImage _waveimg = new WaveImage();
        
    public void draw(HiRes16Color screen, int x, int y) {
        if(y > -IMAGE_H) {
            int tile_x = x > 0 ? (x + 31)%32 - 31 : x%32;
            for(int idx = 0; idx < 8; ++idx) {
                _waveimg.draw(screen, tile_x, y, false, false, true);
                tile_x += IMAGE_W;
            }
        }
    }
}
