
import java.lang.Math;
import java.util.Random;
import femto.Image;
import femto.mode.HiRes16Color;

import sprites.RingSprite;

public class RaceTrack {
    
    private static final int GUIDE_COLOR = 8;
    
    private static final float JUMP_RATE = 0.15;
    
    private static final RingSprite _ring_sprite = new RingSprite();
    
    private Random _rng = new Random();
    private byte[] _track = new byte[64];
    private int _last_ring;
    private float _jump_propability;
    
    private int _ghost_x;
    private int _ghost_y;
    
    public int offsetX = 0;
    
    public void init(int seed) {
        _rng.setSeed(seed);
        
        _jump_propability = 0;
        
        _last_ring = 0;
        _track[0] = 6 + _rng.next(3);
        for(int idx = 1; idx < 64; ++idx) {
            _track[idx] = createRing(_track[idx - 1]);
            if(_track[idx] != 0) _last_ring = idx;
        }
        
        _ghost_x = getRingX(0);
        _ghost_y = getRingY(0);
        
        offsetX = 0;
    }
    
    private byte createRing(int prev) {
        if(prev == 0 || Math.abs(prev) > 4) {
            if(100*_rng.nextFloat() < _jump_propability) {
                _jump_propability = 0;
                return 1 + _rng.next(2);
            }
            else {
                _jump_propability += JUMP_RATE*(100.0 - _jump_propability);
                return 6 + _rng.next(3);
            }
        }
        return 0;
    }
    
    public void step() {
        int idx = (offsetX >> 7)&63;
        if(_track[idx] != 0) {
            _ghost_x = getRingX(idx);
            _ghost_y = getRingY(idx); 
        }
        
        _track[idx] = createRing(_track[idx - 1]);
        if(_track[idx] != 0) {
            _last_ring = 63;
        }
        else {
            _last_ring = _last_ring > 0 ? (_last_ring - 1) : -1;
        }
        
        offsetX += 128;
    }
    
    public int firstRing() {
        int key = (offsetX >> 7)&63;
        return key;
    }
    
    public boolean isRingEnabled(int key) {
        return _track[key] > 0;
    }
    
    public void setRingEnabled(int key, boolean enabled) {
        int val = Math.abs((int)_track[key]);
        _track[key] = enabled ? val : -val;
    }
    
    public int closestRing(int x) {
        int key = (x + 64) >> 7;
        if(key < (offsetX >> 7)) key = offsetX >> 7;
        else if (key > (offsetX >> 7) + 63) key = (offsetX >> 7) + 63;
        return key&63;
    }
    
    public int getRingX(int key) {
        int head = (offsetX >> 7)&63;
        return offsetX + 128*((key - head)&63) + 8;
    }
    
    public int getRingY(int key) {
        return Math.abs(16*_track[key]) + 48;
    }
    
    public void draw(HiRes16Color screen, int x, int y) {
        if(_last_ring == 0) return;
        
        _ring_sprite.setStatic(true);
        x += offsetX;
        
        int head = (offsetX >> 7)&63;
        int ring_idx = head + _last_ring;
        
        int ring_x = x + (ring_idx - head)*128;
        int ring_y = y + 16*_track[ring_idx&63];
        if(ring_y > 0) _ring_sprite.draw(screen, ring_x, ring_y);
        ring_x += 8;
        ring_y = Math.abs(ring_y) + 48;
        
        --ring_idx;
        while(ring_idx >= head) {
            int val = _track[ring_idx&63];
            if(val != 0) {
                int next_x = x + (ring_idx - head)*128 + 8;
                int next_y = y + Math.abs(16*val) + 48;
                screen.drawLine(next_x, next_y, ring_x, ring_y, GUIDE_COLOR, true);
                ring_x = next_x;
                ring_y = next_y;
                if(val > 0) _ring_sprite.draw(screen, ring_x - 8, ring_y - 32);
            }
            --ring_idx;
        }
        screen.drawLine(x - offsetX + _ghost_x, y + _ghost_y, ring_x, ring_y, GUIDE_COLOR, true);
    }
}
