
import java.util.Random;

public class MyMath {
    // Multiplication with rounding
    public static float fmul(float a, float b) {
        float val;
        __inline_cpp__("
            val = FixedPoints::SFixed<23,8>::fromInternal(((std::int64_t)a.getInternal() * b.getInternal() + 128) >> 8);
        ");
        return val;
    }
    
    // Quick and inaccurate sine
    // For the phase 256 corresponds full circle
    public static int qsin(int p) {
        int o = ((p&127)*(-p&127)) >> 5;
        return (p&128) != 0 ? -o : o;
    }
    
    // Quick and inaccurate cosine
    // For the phase 256 corresponds full circle
    public static int qcos(int p) {
        return qsin(p + 64);
    }
    
    private static Random _rng;
    
    public static void setSeed(int seed) {
        if(_rng == null) _rng = new Random();
        _rng.setSeed(seed);
    }

    /// Returns an `int` value, greater than or equal to `min` and less than `max`
    public static int random(int min, int max) {
        if(_rng == null) _rng = new Random();
        
        int bound = max - min;
        int mask = bound - 1; // -1 is important when bound is power of two
        mask |= mask >> 16;
        mask |= mask >> 8;
        mask |= mask >> 4;
        mask |= mask >> 2;
        mask |= mask >> 1;
        
        int ret;
        do {
            ret = _rng.next(31) & mask;
        } while(ret >= bound);
        return min + ret;
    }
}
