
import femto.State;
import femto.Game;
import femto.mode.HiRes16Color;
import femto.input.Button;

import femto.font.FontC64;
import femto.font.FontMonkey;

import sprites.TitleSprite;

public class MenuState extends State {
    
    private static final String[] _items = {"Race", "Time Trial 1", "Time Trial 2", "Time Trial 3"};
    private static final int[] _track_seeds = {167664, 2434240, 61133720};
    private static int[] _track_times = {~(1<<31), ~(1<<31), ~(1<<31)};
    private static int _race_score = 0;
    
    private static int _selected_item = 0;
    
    private static final TitleSprite _title_sprite = new TitleSprite();
    
    private static final int FOCUS_X = 0;
    private static final int FOCUS_Y = 168;
    
    private SkyLayer _sky;
    private OceanLayer _ocean;
    private WavesLayer _waves;
    private Dolphin _dingo;
    
    public MenuState() {
        
    }
    
    public MenuState(RaceState race) {
        if(race != null) {
            _race_score = race.getBestScore();
        }
    }
    
    public MenuState(TimeTrialState time_trial) {
        if(time_trial != null) {
            _track_times[_selected_item - 1] = time_trial.getBestTime();
        }
    }
    
    public void init() {
        System.gc();
        
        _sky = new SkyLayer();
        _ocean = new OceanLayer();
        _waves = new WavesLayer();
        _dingo = new Dolphin(Dolphin.DEMO);
        
        _dingo.setX(0);
        _dingo.setY(288);
        
        _title_sprite.setStatic(true);
        _title_sprite.x = 44;
        _title_sprite.y = 14;
    }

    public void shutdown() {
        _dingo = null;
        _waves = null;
        _ocean = null;
        _sky = null;
    }
    
    // update is called by femto.Game every frame
    public void update() {
        Game.limitFPS(30);
        
        if(Button.Up.justPressed()) {
            _selected_item = _selected_item > 0 ? (_selected_item - 1) : 0;
        }
        if(Button.Down.justPressed()) {
            _selected_item = _selected_item < _items.length - 1 ? (_selected_item + 1) : (_items.length - 1);
        }
        if(Button.A.justPressed()) {
            if(_selected_item == 0) {
                Game.changeState(new RaceState(_race_score));
            }
            else {
                Game.changeState(new TimeTrialState(_track_seeds[_selected_item - 1], _track_times[_selected_item - 1]));
            }
            return;
        }
        
        var screen = Main.screen;
        
        _ocean.update();
        _dingo.update();
        
        int cam_x = FOCUS_X - 110;
        int cam_y = FOCUS_Y - 88;
        screen.cameraX = cam_x;
        screen.cameraY = cam_y;
        
        _sky.draw(screen, -cam_x, 3*32 - cam_y);
        _ocean.draw(screen, -cam_x, 4*32 - cam_y);
        _dingo.draw(screen);
        _waves.draw(screen, -cam_x, 3*32 + 24 - cam_y);
        
        _title_sprite.draw(screen);
        
        screen.fillRect(44, 50, 132, 12, 3);
        
        screen.font = FontC64.font();
        screen.charSpacing = 3;
        screen.textColor = 2;
        String text = "THE DOLPHIN";
        screen.setTextPosition(110 - screen.textWidth(text)/2, 52);
        screen.print(text);
        screen.charSpacing = 1;
        
        drawMenu(screen, _items, _selected_item);
        
        screen.flush();
    }
    
    private void drawMenu(HiRes16Color screen, String[] items, int selected_item) {
        screen.font = FontMonkey.font();
        
        int y = 80;
        for(int idx = 0; idx < items.length; ++idx) {
            if(idx == selected_item) {
                screen.drawLine(66, y - 2, 66 + 88, y - 2, 15);
                screen.drawLine(66, y + screen.textHeight(), 154, y + screen.textHeight(), 15);
                screen.textColor = 15;
            }
            else {
                screen.textColor = 14;
            }
            
            String text = (String)items[idx];
            screen.setTextPosition(110 - screen.textWidth(text)/2, y);
            screen.print(text);
            
            y += screen.textHeight() + 4;
        }
    }
}
