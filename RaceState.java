
import femto.Game;
import femto.State;
import femto.mode.HiRes16Color;
import femto.input.Button;

import femto.font.FontMonkey;

import sprites.MiniRingSprite;
import sprites.GoSprite;
import sprites.PauseSprite;
import sprites.NiceRunSprite;

import sounds.RingPickedSound;
import sounds.RingLostSound;

public class RaceState extends State {
    private SkyLayer _sky;
    private OceanLayer _ocean;
    private WavesLayer _waves;
    private Dolphin _dingo;
    
    private RaceTrack _track;
    
    private static final MiniRingSprite _mini_ring_sprite = new MiniRingSprite();
    private static final GoSprite _go_sprite = new GoSprite();
    private static final PauseSprite _pause_sprite = new PauseSprite();
    private static final NiceRunSprite _nicerun_sprite = new NiceRunSprite();
    
    private static final RingPickedSound _pick_snd = new RingPickedSound(1);
    private static final RingLostSound _lost_snd = new RingLostSound(2);
    
    private static final float _CAM_SPEED = 0.15;
    private float _cam_targetx = 0;
    private float _cam_targety = 0;
    private float _beam_x;
    private float _beam_speed;
    
    private int _score = 0;
    private int _num_missed = 0;
    
    private static final int TEXT_COLOR = 15;
    
    private static final int START = 0, RUN = 1, FINISH = 2, PAUSE = 3;
    private int _state;
    
    private boolean _new_best;
    
    private int _time0;
    private int _race_time;
    private int _pause_time;
    
    private int _best_score;
    
    public RaceState(int best_score) {
        _mini_ring_sprite.setStatic(true);
        _go_sprite.setStatic(true);
        _pause_sprite.setStatic(true);
        _nicerun_sprite.setStatic(true);
        
        _best_score = best_score;
    }
    
    public int getBestScore() {
        return _best_score;
    }
    
    public void init() {
        System.gc();
        
        _sky = new SkyLayer();
        _ocean = new OceanLayer();
        _waves = new WavesLayer();
        _dingo = new Dolphin();
        
        _track = new RaceTrack();
        _track.init(601110512);
        
        _dingo.setX(-80);
        _dingo.setY(_track.getRingY(_track.firstRing()));
        _beam_x = _dingo.getX() - 128;
        _beam_speed = 2.0;
        
        _new_best = false;
        
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
            if((now - _time0 < 2000)) {
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
                if(_score > _best_score) _best_score = _score;
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
                return;
            }
            update_actors = false;
        }
        
        
        if(update_actors) {
            _ocean.update();
            float prev_x = _dingo.getX();
            _dingo.update();
            _beam_x += _beam_speed;
            
            if(_state != FINISH) {
                int key = _track.closestRing((int)_dingo.getX());
                if(_track.isRingEnabled(key)) {
                    int ring_x = _track.getRingX(key);
                    int ring_y = _track.getRingY(key);
                    
                    if((((int)prev_x - ring_x)*((int)_dingo.getX() - ring_x) <= 0) && (_dingo.getY() >= ring_y - 24 && _dingo.getY() < ring_y + 24)) {
                        ++_score;
                        _pick_snd.play();
                        _track.setRingEnabled(key, false);
                        
                        _beam_speed += 0.15*(4.0 - _beam_speed);
                    }
                }
            }
            
            int key = _track.firstRing();
            if(_track.getRingX(key) <= _beam_x) {
                if(_track.isRingEnabled(key)) {
                    ++_num_missed;
                    if(_num_missed <= 3) {
                        _lost_snd.play();
                    }
                }
                _track.step();
                
                if(_num_missed == 3) {
                    _state = FINISH;
                    _time0 = now;
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
            _track.draw(screen, -cam_x, -cam_y);
            
            screen.drawLine((int)_beam_x - cam_x, 0, (int)_beam_x - cam_x, 176, 13);
            screen.drawLine((int)_beam_x - cam_x + 165, 0, (int)_beam_x - cam_x + 165, 176, 12);
            
            _dingo.draw(screen);
            
            _waves.draw(screen, -cam_x, 3*32 + 24 - cam_y);
        }
        
        screen.font = FontMonkey.font();
        screen.textColor = TEXT_COLOR;
        
        if(_state == FINISH) {
            _nicerun_sprite.draw(screen, 110 - 120/2, 66 - 22);
            
            int time = System.currentTimeMillis() - _time0;
            String text = (_score > _best_score ? "New best " : "Score ") + _score;
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
            
            String text = String.valueOf(_score);
            screen.setTextPosition(220 - screen.textWidth(text), 176 - screen.textHeight());
            screen.print(text);
        }
        
        int score = _state == FINISH ? Math.max(_best_score, _score) : _best_score;
        if(score > 0) {
            screen.textColor = 14;
            String text = "Best " + score;
            screen.setTextPosition(0, 176 - screen.textHeight());
            screen.print(text);
        }
        
        for(int idx = 3 - _num_missed; idx > 0; --idx) {
            _mini_ring_sprite.draw(screen, 110 - 24 + 10*idx, 0);
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