
import femto.Game;
import femto.mode.HiRes16Color;
import femto.sound.Mixer;

import femto.palette.Europa16;

import femto.font.TIC80;

public class Main {
    public static final HiRes16Color screen = new HiRes16Color(Europa16.palette(), TIC80.font());
    
    public static void main(String[] args) {
        Mixer.init(11025);
        
        // Start the game using MenuState as the initial state
        Game.run(TIC80.font(), new MenuState());
    }
}
