
import java.lang.Math;
import java.util.Random;
import femto.Image;
import femto.mode.HiRes16Color;

import sprites.RingSprite;

public class TimeTrialTrack {
    private static final int GUIDE_COLOR = 8;
    
    private static final RingSprite _ring_sprite = new RingSprite();
    
    private int _num_rings;

    private byte _track[] = new byte[48];
    
    public TimeTrialTrack() {
        
    }
    
    public void init(int seed) {
        Random rng = new Random();
        rng.setSeed(seed);
        
        int val = rng.next(4) - 7;
        this._track[0] = val > 0 ? val : 1 - val;
        this._num_rings = 1;
        
        for(int idx = 1; idx < 48; ++idx) {
            val = rng.next(4) - 7;
            this._track[idx] = val > 0 ? val : 0;
            
            if(this._track[idx] > 0) ++this._num_rings;
        }
    }
    
    public boolean isRingEnabled(int idx) {
        return _track[idx] > 0;
    }
    
    public void setRingEnabled(int idx, boolean val) {
        if(val) {
            if(this._track[idx] < 0) {
                ++this._num_rings;
                this._track[idx] = -this._track[idx];
            }
        }
        else {
            if(this._track[idx] > 0) {
                --this._num_rings;
                this._track[idx] = -this._track[idx];
            }
        }
    }
    
    public int numRings() {
        return this._num_rings;
    }
    
    public int getRingY(int idx) {
        return 32*Math.abs((int)_track[idx]) + 32;
    }
    
    public void update() {
        
    }
    
    public void draw(HiRes16Color screen) {
        int next = _track.length - 1;
        while(_track[next] == 0 && next > 0) --next;
        int next_y = 32*Math.abs((int)_track[next]);
        
        if(_track[next] > 0) _ring_sprite.draw(screen, 128*next, next_y);
        
        for(int idx = next - 1; idx >= 0; --idx) {
            if(_track[idx] != 0) {
                int x = 128*idx;
                int y = 32*Math.abs((int)this._track[idx]);
                
                screen.drawLine(x + 8, y + 32, 128*next + 8, next_y + 32, GUIDE_COLOR, false);
                if(_track[idx] > 0) _ring_sprite.draw(screen, x, y);
                
                next = idx;
                next_y = y;
            }
        }
    }
}
