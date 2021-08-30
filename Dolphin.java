
import java.util.Math;

import femto.mode.HiRes16Color;
import femto.input.Button;

import sprites.DolphinSprite;

import sounds.SplashSound;

public class Dolphin {
    private static final int BOOST_COLOR = 3, STRIPES_COLOR = 1;
    
    private static final DolphinSprite _sprite = new DolphinSprite();
    private static final SplashSample _splash_snd = new SplashSample(0);
    
    private static final float _AGILITY = 0.15;
    private static final float _ACCEL = 6;
    private static final float _BOOST_ACCEL = 12;
    
    private static final float _DTIME = 1.0/30;
    private static final float _LIFT_COEF = 400;
    private static final float _DRAG_COEF = 25;
    
    static final int PLAYER = 0, DEMO = 1;
    private int _mode = PLAYER;
    
    private float _posx;
    private float _posy;
    private float _rotation = 0;
    private float _fwdx;
    private float _fwdy;
    
    private float _boost = 100;
    private boolean _boost_active = false;
    private boolean _swim = false;
    private boolean _underwater = true;
    
    private float _x = 0;
    private float _y = 0;
    
    private int _target_x;
    private int _target_y;
    
    float vx = 0;
    float vy = 0;
    
    public Dolphin() {
        
    }
    
    public Dolphin(int mode) {
        _mode = mode;
        if(_mode == DEMO) {
            _target_x = 0;
            _target_y = 96;
        }
    }
    
    public float getX() {
        return _x;
    }
    
    public void setX(float x) {
        _x = x;
        _posx = x/16;
    }
    
    public float getY() {
        return _y;
    }
    
    public void setY(float y) {
        _y = y;
        _posy = -y/16;
        
        _underwater = (_y > 96 + 24);
    }
    
    public void update() {
        if(_mode == PLAYER) {
            player();
        }
        else if(_mode == DEMO) {
            demo();
        }
    }
    
    private void demo() {
        int dx = _target_x - (int)_x;
        int dy = _target_y - (int)_y;
        int dist_sq = dx*dx + dy*dy;
        if(dist_sq < 1024) {
            _target_x = Math.random(-100, 100);
            _target_y = _target_y < 128 ? Math.random(1, 6) : Math.random(0, 6);
            _target_y = _target_y > 0 ? (192 + 32*_target_y) : 96;
        }
        
        int updn = _target_y > (_y + 16) ? 1 : (_target_y < (_y - 16) ? -1 : 0);
        int ltrt = _target_x > (_x + 16) ? 1 : (_target_x < (_x - 16) ? -1 : 0);
        _swim = (updn | ltrt) != 0;
        _boost_active = _target_y < 128; // use boost when target point is above water
        
        if(_swim) {
            float delta = Math.atan2(-updn, ltrt)*(180/Math.PI) - _rotation;
            if(delta > 180) delta -= 360;
            else if(delta < -180) delta += 360;
            
            _rotation += MyMath.fmul(delta, _AGILITY);
            if(delta < 0) {
                if(_rotation < -180) _rotation += 360;
            }
            else {
                if(_rotation > 180) _rotation -= 360;
            }
        }
        
        float radians = (_rotation*Math.PI)/180;
        _fwdx = Math.cos(radians);
        _fwdy = Math.sin(radians);
        
        float vel_fwd = _fwdx*this.vx + _fwdy*this.vy;
        float vel_up = -_fwdy*this.vx + _fwdx*this.vy;
        
        float dvelx = 0;
        float dvely = 0;
        
        if(_y <= 96 + 24 + 4) {
            _boost += 100 * _DTIME;
            if(_boost > 100) _boost = 100;
        }
        
        if(_y > 96 + 24) {
            if(!_underwater) {
                _underwater = true;
                float vol = 0.1 + 0.09*Math.abs(vel_up);
                _splash_snd.volume = vol < 1.0 ? vol : 1.0;
                _splash_snd.play();
            }
            
            if(_boost_active&_swim) _boost -= 25 * _DTIME;
            if(_boost < 0) {
                _boost = 0;
                _boost_active = false;
            }
            
            float dvel_up = MyMath.fmul(vel_up, 256 - Math.abs(vel_up)*13.65325)/256 - vel_up;
            dvelx += MyMath.fmul(-_fwdy, dvel_up);
            dvely += MyMath.fmul(_fwdx, dvel_up);
            
            float dvel_fwd = MyMath.fmul(vel_fwd, 256 - Math.abs(vel_fwd)*0.85325)/256 - vel_fwd;
            dvelx += MyMath.fmul(_fwdx, dvel_fwd);
            dvely += MyMath.fmul(_fwdy, dvel_fwd);
            
            if((updn | ltrt) != 0) {
                float accel = (_boost_active&_swim) ? _BOOST_ACCEL : _ACCEL;
                dvelx += _fwdx * accel * _DTIME;
                dvely += _fwdy * accel * _DTIME;
            }
            else {
                _boost_active = false;
            }
        }
        else {
            if(_underwater) {
                _underwater = false;
                float vol = 0.1 + 0.09*Math.abs(vel_up);
                _splash_snd.volume = vol < 1.0 ? vol : 1.0;
                _splash_snd.play();
            }
            
            _swim = false;
            _boost_active = false;
            dvely = -9.81 * _DTIME;
        }
        
        _posx += (this.vx + dvelx/2) * _DTIME;
        _posy += (this.vy + dvely/2) * _DTIME;
        this.vx += dvelx;
        this.vy += dvely;
        
        _x = 16*_posx;
        _y = -16*_posy;
    }
    
    private void player() {
        int updn = (Button.Down.isPressed() ? 1 : 0) - (Button.Up.isPressed() ? 1 : 0);
        int ltrt = (Button.Right.isPressed() ? 1 : 0) - (Button.Left.isPressed() ? 1 : 0);
        _swim = (updn | ltrt) != 0;
        _boost_active = Button.A.isPressed();
        
        if(_swim) {
            float delta = Math.atan2(-updn, ltrt)*(180/Math.PI) - _rotation;
            if(delta > 180) delta -= 360;
            else if(delta < -180) delta += 360;
            
            _rotation += MyMath.fmul(delta, _AGILITY);
            if(delta < 0) {
                if(_rotation < -180) _rotation += 360;
            }
            else {
                if(_rotation > 180) _rotation -= 360;
            }
        }
        
        float radians = (_rotation*Math.PI)/180;
        _fwdx = Math.cos(radians);
        _fwdy = Math.sin(radians);
        
        float vel_fwd = _fwdx*this.vx + _fwdy*this.vy;
        float vel_up = -_fwdy*this.vx + _fwdx*this.vy;
        
        float dvelx = 0;
        float dvely = 0;
        
        if(_y <= 96 + 24 + 4) {
            _boost += 100 * _DTIME;
            if(_boost > 100) _boost = 100;
        }
        
        if(_y > 96 + 24) {
            if(!_underwater) {
                _underwater = true;
                float vol = 0.1 + 0.09*Math.abs(vel_up);
                _splash_snd.volume = vol < 1.0 ? vol : 1.0;
                _splash_snd.play();
            }
            
            if(_boost_active&_swim) _boost -= 25 * _DTIME;
            if(_boost < 0) {
                _boost = 0;
                _boost_active = false;
            }
            
            float dvel_up = MyMath.fmul(vel_up, 256 - Math.abs(vel_up)*13.65325)/256 - vel_up;
            dvelx += MyMath.fmul(-_fwdy, dvel_up);
            dvely += MyMath.fmul(_fwdx, dvel_up);
            
            float dvel_fwd = MyMath.fmul(vel_fwd, 256 - Math.abs(vel_fwd)*0.85325)/256 - vel_fwd;
            dvelx += MyMath.fmul(_fwdx, dvel_fwd);
            dvely += MyMath.fmul(_fwdy, dvel_fwd);
            
            if((updn | ltrt) != 0) {
                float accel = (_boost_active&_swim) ? _BOOST_ACCEL : _ACCEL;
                dvelx += _fwdx * accel * _DTIME;
                dvely += _fwdy * accel * _DTIME;
            }
            else {
                _boost_active = false;
            }
        }
        else {
            if(_underwater) {
                _underwater = false;
                float vol = 0.1 + 0.09*Math.abs(vel_up);
                _splash_snd.volume = vol < 1.0 ? vol : 1.0;
                _splash_snd.play();
            }
            
            _swim = false;
            _boost_active = false;
            dvely = -9.81 * _DTIME;
        }
        
        _posx += (this.vx + dvelx/2) * _DTIME;
        _posy += (this.vy + dvely/2) * _DTIME;
        this.vx += dvelx;
        this.vy += dvely;
        
        _x = 16*_posx;
        _y = -16*_posy;
    }
    
    public void draw(HiRes16Color screen) {
        int anim = (4 - (int)(0.5 + _rotation*(8/180.0)))&15;
        setAnim(anim);
        _sprite.endFrame = _sprite.startFrame + (_swim ? 1 : 0);
        _sprite.setMirrored(anim > 8);
        _sprite.setFlipped(anim == 8);
        
        if(_boost > 0) {
            screen.drawCircle(_x, _y, 8 + (int)(0.16*_boost), BOOST_COLOR, false);
        }
        
        _sprite.draw(screen, _x - 16, _y - 16);
        
        if(_boost_active) {
            float radians = (_rotation*Math.PI)/180;
            
            for(int idx = 0; idx < 3; ++idx) {
                int phase = (int)((radians + Math.PI/2)*128/Math.PI) + MyMath.random(0, 128);
                float x0 = _x + ((float)MyMath.qcos(phase))/16;
                float y0 = _y - ((float)MyMath.qsin(phase))/16;
                screen.drawLine(x0, y0, x0 - 24*_fwdx, y0 + 24*_fwdy, STRIPES_COLOR, false);
            }
        }
    }
    
    private void setAnim(int anim) {
        switch(anim < 8 ? anim : ((16 - anim)&7)) {
            case 0:
                _sprite.north();
                break;
            case 1:
                _sprite.nne();
                break;
            case 2:
                _sprite.ne();
                break;
            case 3:
                _sprite.nee();
                break;
            case 4:
                _sprite.east();
                break;
            case 5:
                _sprite.see();
                break;
            case 6:
                _sprite.se();
                break;
            case 7:
                _sprite.sse();
                break;
        }
    }
}

private class SplashSample extends SplashSound {
    
    public float volume = 1.0;
    
    public SplashSample(char channel) {
        super(channel);
    }
    
    public void reset() {
        super.reset();
    }
    
    ubyte update() {
        // Read current sample
        int val = (int)super.update() - 128;
        
        // Amplify the sample value
        val = (int)(val * this.volume) + 128;
        if(val < 0) {
            return 0;
        }
        else if(val > 255) {
            return 255;
        }
        return (ubyte)val;
    }
}
