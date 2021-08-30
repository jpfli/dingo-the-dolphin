
import femto.Game;
import femto.State;
import femto.mode.HiRes16Color;
import femto.input.Button;

import femto.font.FontMonkey;

import sprites.RingSprite;
import sprites.GoSprite;
import sprites.FinishSprite;
import sprites.PauseSprite;

import sounds.RingPickedSound;

public class TimeTrialState extends State {
    private SkyLayer _sky;
    private OceanLayer _ocean;
    private WavesLayer _waves;
    private Dolphin _dingo;
    private TimeTrialTrack _track;
    
    private static final GoSprite _go_sprite = new GoSprite();
    private static final FinishSprite _finish_sprite = new FinishSprite();
    private static final PauseSprite _pause_sprite = new PauseSprite();
    
    private static final RingPickedSound _pick_snd = new RingPickedSound(1);
    
    private static final float _CAM_SPEED = 0.15;
    private float _cam_targetx = 0;
    private float _cam_targety = 0;
    
    // private static final int TEXT_COLOR = 5;  // Na16
    // private static final int TEXT_COLOR = 13; // Cthulhu
    private static final int TEXT_COLOR = 15; // Europa16
    
    private static final int START = 0, RUN = 1, FINISH = 2, PAUSE = 3;
    private int _state;
    
    // private boolean _new_best;
    
    private int _time0;
    private int _race_time;
    private int _pause_time;
    private int _best_time;
    
    private static int _track_seed = 0;
    
    public TimeTrialState(int seed, int best_time) {
        _go_sprite.setStatic(true);
        _finish_sprite.setStatic(true);
        _pause_sprite.setStatic(true);
        
        _track_seed = seed;
        _best_time = best_time;
    }
    
    public int getBestTime() {
        return _best_time;
    }
    
    public void init() {
        System.gc();
        
        _sky = new SkyLayer();
        _ocean = new OceanLayer();
        _waves = new WavesLayer();
        _dingo = new Dolphin();
        _track = new TimeTrialTrack();
        
        _track.init(_track_seed);
        
        _dingo.setX(-80);
        int y = _track.getRingY(0);
        _dingo.setY(y < 160 ? 160 : y);
        System.out.println("y:" + _dingo.getY());
        
        _state = START;
        _time0 = System.currentTimeMillis();
        _race_time = -1;
    }
    
    public void shutdown() {
        _track = null;
        _dingo = null;
        _waves = null;
        _ocean = null;
        _sky = null;
    }
    
    // update is called by femto.Game every frame
    public void update() {
        Game.limitFPS(30);
        
        int now = System.currentTimeMillis();
        
        boolean update_actors = true;
        
        if(_state == RUN) {
            if(Button.C.justPressed()) {
                _state = PAUSE;
                _pause_time = now;
                update_actors = false;
            }
            else {
                _race_time = now - _time0;
            }
        }
        else if(_state == START) {
            if(now - _time0 < 2000) {
                update_actors = false;
            }
            else if(Button.C.justPressed()) {
                _state = PAUSE;
                _pause_time = now;
                update_actors = false;
            }
        }
        else if(_state == FINISH) {
            if(Button.C.justPressed()) {
                if(_race_time < _best_time) _best_time = _race_time;
                Game.changeState(new MenuState(this));
                return;
            }
        }
        else if(_state == PAUSE) {
            if(Button.A.justPressed()) {
                _state = _race_time < 0 ? START : RUN;
                _time0 += now - _pause_time;
            }
            else if(Button.C.justPressed()) {
                Game.changeState(new MenuState());
            }
            update_actors = false;
        }
        
        
        if(update_actors) {
            _ocean.update();
            float prev_x = _dingo.getX();
            _dingo.update();
            
            if(_state != FINISH) {
                int ring_idx = ((int)_dingo.getX() + 64)/128;
                if(_track.isRingEnabled(ring_idx)) {
                    int ring_x = 128*ring_idx + 8;
                    int ring_y = _track.getRingY(ring_idx);
                    
                    if((((int)prev_x - ring_x)*((int)_dingo.getX() - ring_x) <= 0) && (_dingo.getY() >= ring_y - 24 && _dingo.getY() < ring_y + 24)) {
                        _pick_snd.play();
                        _track.setRingEnabled(ring_idx, false);
                        
                        if(_state == START) {
                            _state = RUN;
                            _time0 = System.currentTimeMillis();
                            _race_time = 0;
                        }
                        else if(_track.numRings() == 0) {
                            _state = FINISH;
                            _time0 = now;
                        }
                    }
                }
            }
        }
        
        draw();
    }
    
    private void draw() {
        var screen = Main.screen;
        
        if(_state != PAUSE) {
            _cam_targetx += (7*_dingo.vx - _cam_targetx) * _CAM_SPEED;
            _cam_targety += (7*_dingo.vy - _cam_targety) * _CAM_SPEED;
            int cam_x = (int)(_dingo.getX() + _cam_targetx) - 110;
            int cam_y = (int)(_dingo.getY() - _cam_targety) - 88;
            screen.cameraX = cam_x;
            screen.cameraY = cam_y;
            
            _sky.draw(screen, -cam_x, 3*32 - cam_y);
            _ocean.draw(screen, -cam_x, 4*32 - cam_y);
            _track.draw(screen);
            
            _dingo.draw(screen);
            
            _waves.draw(screen, -cam_x, 3*32 + 24 - cam_y);
        }
        
        screen.font = FontMonkey.font();
        screen.textColor = TEXT_COLOR;
        
        if(_state == FINISH) {
            _finish_sprite.draw(screen, 110 - 83/2, 66 - 22);
            
            int time = System.currentTimeMillis() - _time0;
            String text = ((_race_time < _best_time) ? "New best " : "Time ") + timeToString(_race_time);
            if(time >= 2000 || ((time/200)&1) != 0) {
                screen.setTextPosition(110 - screen.textWidth(text)/2, 110);
                screen.print(text);
            }
            
            text = "C to exit";
            screen.setTextPosition(110 - screen.textWidth(text)/2, 110 + 2*screen.textHeight());
            screen.print(text);
        }
        else {
            if(_state == START) {
                int time = System.currentTimeMillis() - _time0;
                if(time < 2000 && ((time/200)&1) != 0) {
                    _go_sprite.draw(screen, 110 - 41/2, 66 - 22);
                }
            }
            else if(_state == PAUSE) {
                _pause_sprite.draw(screen, 110 - 83/2, 66 - 22);
                
                String text = "A to continue";
                screen.setTextPosition(110 - screen.textWidth(text)/2, 110);
                screen.print(text);
                
                text = "C to exit";
                screen.setTextPosition(110 - screen.textWidth(text)/2, 110 + 2*screen.textHeight());
                screen.print(text);
            }
            
            String text = timeToString(_race_time);
            screen.setTextPosition(220 - screen.textWidth(text), 176 - screen.textHeight());
            screen.print(text);
        }
        
        int time = _state == FINISH ? Math.min(_best_time, _race_time) : _best_time;
        if(time < ~(1<<31)) {
            String text = "Best " + timeToString(time);
            screen.setTextPosition(0, 176 - screen.textHeight());
            screen.textColor = 14;
            screen.print(text);
        }
        
        screen.flush();
    }
    
    private String timeToString(int millis) {
        int secs = millis/1000;
        int mins = secs/60;
        secs -= 60*mins;
        return String.valueOf(mins) + (secs < 10 ? ":0" : ":") + String.valueOf(secs) + "." + String.valueOf((millis - 1000*(60*mins + secs))/100);
    }
}
