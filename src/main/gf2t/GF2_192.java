package gf2t;

public class GF2_192 {
    private long [] word = new long[3];

    /**
     *
     * @param that
     * @return true if and only if this and that represent the same field element
     */
    public boolean equals(GF2_192 that) {
        return this.word[0]==that.word[0] && this.word[1]==that.word[1] && this.word[2]==that.word[2];
    }

    // using irreducible polynomial x^192+x^7+x^2+x+1
    // We need only the last word
    static private final long irredPentanomial = (1L<<7) | (1L<<2) | (1L<<1) | 1L;

    // irredPentanomial times 0, 1, x, x+1, x^2, x^2+1, x^2+x, x^2+x+1, x^3, x^3+1, x^3+x, x^3+x+1, x^3+x^2, x^3+x^2+1, x^3+x^2+x, x^3+x^2x+1,
    // Need only the last word, because the leading two words are 0
    static private final long [] irredMuls = {0L, irredPentanomial, irredPentanomial<<1, (irredPentanomial<<1)^irredPentanomial,
            irredPentanomial<<2, (irredPentanomial<<2)^irredPentanomial, (irredPentanomial<<2)^(irredPentanomial<<1), (irredPentanomial<<2)^(irredPentanomial<<1)^irredPentanomial,
            irredPentanomial<<3, (irredPentanomial<<3)^irredPentanomial, (irredPentanomial<<3)^(irredPentanomial<<1), (irredPentanomial<<3)^(irredPentanomial<<1)^irredPentanomial,
            (irredPentanomial<<3)^(irredPentanomial<<2), (irredPentanomial<<3)^(irredPentanomial<<2)^irredPentanomial, (irredPentanomial<<3)^(irredPentanomial<<2)^(irredPentanomial<<1), (irredPentanomial<<3)^(irredPentanomial<<2)^(irredPentanomial<<1)^irredPentanomial

    };

    /**
     * returns the 0 field element
     */
    public GF2_192() {
    }

    /**
     * returns a copy of the field element
     * @param that
     */
    public GF2_192(GF2_192 that) {
        this.word[0] = that.word[0];
        this.word[1] = that.word[1];
        this.word[2] = that.word[2];
    }

    /**
     * returns the field element whose 32 least significant bits are bits of that and rest are 0
     * @param that
     */
    public GF2_192(int that) {
        this.word[0] = ((long) that) & 0xFFFFFFFFL;
    }

    /**
     * returns the field element whose bits are given by the long array
     * @param that must be length 3
     */
    public GF2_192(long [] that) {
        assert (that.length == 3);
        this.word[0] = that[0];
        this.word[1] = that[1];
        this.word[2] = that[2];
    }

    /**
     * returns the field element whose bits are given by the byte array
     * @param that must be length 24
     */
    public GF2_192(byte [] that) {
        assert (that.length == 24);
        for (int i = 0; i<8; i++) {
            word[0] |= (((long)that[i] & 0xFF))<<(i<<3);
        }
        for (int i = 0; i<8; i++) {
            word[1] |= (((long)that[i+8] & 0xFF))<<(i<<3);
        }
        for (int i = 0; i<8; i++) {
            word[2] |= (((long)that[i+16] & 0xFF))<<(i<<3);
        }
    }

    /**
     *
     * @return long array of length 3 containing the two words of the field element
     */
    public long [] toLongArray() {
        long [] ret = new long[3];
        ret[0] = word[0];
        ret[1] = word[1];
        ret[2] = word[2];
        return ret;
    }


    /**
     *
     * @return true if this == 0, false otherwise
     */
    public boolean isZero () {
        return word[0]==0L && word[1]==0L && word[2]==0L;
    }

    /**
     *
     * @return true if this == 1, false otherwise
     */
    public boolean isOne () {
        return word[0]==1L && word[1]==0L && word[2]==0L;
    }

    /**
     * Computes a plus b and puts the result into res.
     * @param res output; must be not null; may be equal to a and/or b
     * @param a multiplicand; may be equal to res, in which case will get overwritten
     * @param b multiplier; may be equal to res, in which case will get overwritten
     */

    public static void add (GF2_192 res, GF2_192 a, GF2_192 b) {
        res.word[0] = a.word[0]^b.word[0];
        res.word[1] = a.word[1]^b.word[1];
        res.word[2] = a.word[2]^b.word[2];
    }

    /**
     * Computes a times b and puts the result into res. More efficient than mul(res, a, new GF2_192(b))
     * @param res output; must be not null; may be equal to a and/or b
     * @param a multiplicand; may be equal to res, in which case will get overwritten
     * @param b multiplier; may be equal to res, in which case will get overwritten
     */

    public static void mul (GF2_192 res, GF2_192 a, byte b) {

        // contains a*0, a*1, a*x, a*(x+1), mod reduced
        long [] a0muls = new long[4];
        long [] a1muls = new long[4];
        long [] a2muls = new long[4];

        a0muls[1] = a.word[0];
        a1muls[1] = a.word[1];
        a2muls[1] = a.word[2];

        // a*x
        a0muls[2] = a.word[0] << 1;
        a1muls[2] = (a.word[1] << 1) | (a.word[0]>>>63);
        a2muls[2] = (a.word[2] << 1) | (a.word[1]>>>63);
        // mod reduce
        a0muls[2] ^= irredMuls[(int)(a.word[2]>>>63)];

        // a*x+a
        a0muls[3] = a0muls[1] ^ a0muls[2];
        a1muls[3] = a1muls[1] ^ a1muls[2];
        a2muls[3] = a2muls[1] ^ a2muls[2];


        int index = (b>>>6) & 3;
        long w0 = a0muls[index], w1 = a1muls[index], w2 = a2muls[index];

        for (int i = 4; i >= 0; i -= 2) {
            // Multiply by x^2
            int modReduceIndex = (int) (w2 >>> 62);
            w2 = (w2 << 2) | (w1 >>> 62);
            w1 = (w1 << 2) | (w0 >>> 62);
            // MOD REDUCE ACCORDING TO modReduceIndex by XORing the right value
            w0 = (w0 << 2) ^ irredMuls[modReduceIndex];

            // Add the correct multiple of a
            index =  (b >>> i) & 3;
            w0 ^= a0muls[index];
            w1 ^= a1muls[index];
            w2 ^= a2muls[index];
        }
        res.word[0] = w0;
        res.word[1] = w1;
        res.word[2] = w2;
    }

    /**
     * Computes a times b and puts the result into res.
     * @param res output; must be not null; may be equal to a and/or b
     * @param a multiplicand; may be equal to res, in which case will get overwritten
     * @param b multiplier; may be equal to res, in which case will get overwritten
     */
    public static void mul (GF2_192 res, GF2_192 a, GF2_192 b) {

        // Implements a sort of times-x-and-add algorithm, except instead of multiplying by x
        // we multiply by x^4 and then add one of possible 16 precomputed values

        // contains a*0, a*1, a*x, a*(x+1), a*x^2, a*(x^2+1), a*(x^2+x), a*(x^2+x+1)
        // a*x^3, a*(x^3+1), a*(x^3+x), a*(x^3+x+1), a*(x^3+x^2), a*(x^3+x^2+1), a*(x^3+x^2+x), a*(x^3+x^2+x+1), all mod reduced
        // First word of each is in a0 muls, second word of each is in a1muls, third word of each is in a2muls
        long [] a0muls = new long[16];
        long [] a1muls = new long[16];
        long [] a2muls = new long[16];

        // a0muls[0], a1muls[0] and a2muls[0] are already correctly initialized to 0

        a0muls[1] = a.word[0];
        a1muls[1] = a.word[1];
        a2muls[1] = a.word[2];

        // a*x, a*x^2, a*x^3
        for (int i = 2; i<=8; i*=2) {
            // multiply a*x^{log_2 i/2} by x to get a*x^{log_2 i}
            int prev = i / 2;
            a0muls[i] = a0muls[prev] << 1;
            a1muls[i] = (a1muls[prev] << 1) | (a0muls[prev] >>> 63);
            a2muls[i] = (a2muls[prev] << 1) | (a1muls[prev] >>> 63);
            // mod reduce
            a0muls[i] ^= irredMuls[(int) (a2muls[prev] >>> 63)];
        }

        // a*(x+1)
        a0muls[3] = a0muls[1] ^ a0muls[2];
        a1muls[3] = a1muls[1] ^ a1muls[2];
        a2muls[3] = a2muls[1] ^ a2muls[2];


        // a*(x^2+1), a*(x^2+x), a*(x^2+x+1)
        for (int i = 1; i<4; i++) {
            a0muls[4|i] = a0muls[4]^a0muls[i];
            a1muls[4|i] = a1muls[4]^a1muls[i];
            a2muls[4|i] = a2muls[4]^a2muls[i];
        }

        // a*(x^3+1), a*(x^3+x), a*(x^3+x+1), a*(x^3+x^2), a*(x^3+x^2+1), a*(x^3+x^2+x), a*(x^3+x^2+x+1)
        for (int i = 1; i<8; i++) {
            a0muls[8|i] = a0muls[8]^a0muls[i];
            a1muls[8|i] = a1muls[8]^a1muls[i];
            a2muls[8|i] = a2muls[8]^a2muls[i];
        }

        long w0 = 0, w1 = 0, w2 = 0;
        for (int j = 2; j>=0; j--) {
            long multiplier = b.word[j];
            for (int i = 60; i >= 0; i -= 4) {
                // Multiply by x^4
                int modReduceIndex = (int) (w2 >>> 60);
                w2 = (w2 << 4) | (w1 >>> 60);
                w1 = (w1 << 4) | (w0 >>> 60);
                // MOD REDUCE ACCORDING TO modReduceIndex by XORing the right value
                w0 = (w0 << 4) ^ irredMuls[modReduceIndex];

                // Add the correct multiple of a
                int index = (int) ((multiplier >>> i) & 15);
                w0 ^= a0muls[index];
                w1 ^= a1muls[index];
                w2 ^= a2muls[index];
            }
        }
        res.word[0] = w0;
        res.word[1] = w1;
        res.word[2] = w2;
    }


    /**
     * Computes z^{-1} and puts the result into res.
     * @param res output; must be not null; may be equal to z
     * @param z input to be raised to 2^16; may be equal to res, in which case will get overwritten
     */

    public static void invert (GF2_192 res, GF2_192 z) {
        // Computes z^{2^192-2} = z^{exponent written in binary as 191 ones followed by a 0}
        // (by Fermat's little theorem, this is the correct inverse)

        // contains x raised to the power whose binary representation is 2^i ones
        GF2_192 zTo2i1s = new GF2_192(z);

        sqr(res, z); // res = z^2
        // contains x raised to the power whose binary representation is 2^i ones followed by 2^i zeros
        GF2_192 zTo2i1s2i0s = new GF2_192(res);


        int twoToi = 2;
        for (int i = 1; i<=3; i++) {
            mul(zTo2i1s, zTo2i1s2i0s, zTo2i1s);

            // Now compute aTo2i1s2i0s = aTo2i1s with 2^i zeros appended to the exponent
            sqr(zTo2i1s2i0s, zTo2i1s);
            for (int j = 1; j < twoToi; j++) {
                sqr(zTo2i1s2i0s, zTo2i1s2i0s);
            }

            mul(res, res, zTo2i1s2i0s);
            // above line makes res = z raised to exponent that has 2^{i+1} bits, all 1s except last 0

            twoToi *= 2;
        }


        // compute for i = 4
        mul(zTo2i1s, zTo2i1s2i0s, zTo2i1s);
        // aTo2i1s2i0s = aTo2i1s with 16 zeros appended to the exponent
        pow65536(zTo2i1s2i0s, zTo2i1s);

        mul (res, res, zTo2i1s2i0s);
        //  above line makes res = z  raised to exponent that has 32 bits, all 1s except last 0

        // compute for i = 5
        mul(zTo2i1s, zTo2i1s2i0s, zTo2i1s);
        // aTo2i1s2i0s = aTo2i1s with 32 zeros appended to the exponent
        pow65536(zTo2i1s2i0s, zTo2i1s);
        pow65536(zTo2i1s2i0s, zTo2i1s2i0s);
        mul (res, res, zTo2i1s2i0s);
        // res is now raised to exponent that has 64 bits, all 1s except last 0

        // compute for i = 6
        mul(zTo2i1s, zTo2i1s2i0s, zTo2i1s);
        // The fol\lowing four lines will make aTo2i1s2i0s = aTo2i1s with 64 zeros appended to the exponent
        pow65536(zTo2i1s2i0s, zTo2i1s);
        pow65536(zTo2i1s2i0s, zTo2i1s2i0s);
        pow65536(zTo2i1s2i0s, zTo2i1s2i0s);
        pow65536(zTo2i1s2i0s, zTo2i1s2i0s);

        // Now compute z to exponent 128 1s followed by 64 0s (in binary)
        mul(zTo2i1s, zTo2i1s2i0s, zTo2i1s); // this computes z to exponent of 128 ones (in binary)
        // The following four lines will add 64 0s to the exponent (in binary)
        GF2_192 t = new GF2_192();
        pow65536(t, zTo2i1s);
        pow65536(t, t);
        pow65536(t, t);
        pow65536(t, t);

        mul (res, res, t);
        //  above line makes res = z raised to exponent that has 191 bits, all 1s except last 0
    }


    public static void sqr (GF2_192 res, GF2_192 z) {
        mul(res, z, z);
    }
    public static void pow65536 (GF2_192 res, GF2_192 z) {
        sqr(res, z);
        for (int k=1; k<16; k++) {
            sqr(res, res);
        }

    }



        /* These tables are needed for the sqr function -- see explanation in the code sqr */
    /* They take up less than 3 KB */

    // byte0SquaresTable[i] contains the trailing 16 bits of the square of the field element that has i in the last byte (byte 0) of word 0 (and 0s elsewhere).
    // The remaining bits of the square are always 0.
    // The table is all positive (leading bit 0), so conversion to long won't cause a problem -- it will simply prepend 0s
    private static final short [] byte0SquaresTable = {0, 1, 4, 5, 16, 17, 20, 21, 64, 65, 68, 69, 80, 81, 84, 85, 256, 257, 260, 261, 272, 273, 276, 277, 320, 321, 324, 325, 336, 337, 340, 341, 1024, 1025, 1028, 1029, 1040, 1041, 1044, 1045, 1088, 1089, 1092, 1093, 1104, 1105, 1108, 1109, 1280, 1281, 1284, 1285, 1296, 1297, 1300, 1301, 1344, 1345, 1348, 1349, 1360, 1361, 1364, 1365, 4096, 4097, 4100, 4101, 4112, 4113, 4116, 4117, 4160, 4161, 4164, 4165, 4176, 4177, 4180, 4181, 4352, 4353, 4356, 4357, 4368, 4369, 4372, 4373, 4416, 4417, 4420, 4421, 4432, 4433, 4436, 4437, 5120, 5121, 5124, 5125, 5136, 5137, 5140, 5141, 5184, 5185, 5188, 5189, 5200, 5201, 5204, 5205, 5376, 5377, 5380, 5381, 5392, 5393, 5396, 5397, 5440, 5441, 5444, 5445, 5456, 5457, 5460, 5461, 16384, 16385, 16388, 16389, 16400, 16401, 16404, 16405, 16448, 16449, 16452, 16453, 16464, 16465, 16468, 16469, 16640, 16641, 16644, 16645, 16656, 16657, 16660, 16661, 16704, 16705, 16708, 16709, 16720, 16721, 16724, 16725, 17408, 17409, 17412, 17413, 17424, 17425, 17428, 17429, 17472, 17473, 17476, 17477, 17488, 17489, 17492, 17493, 17664, 17665, 17668, 17669, 17680, 17681, 17684, 17685, 17728, 17729, 17732, 17733, 17744, 17745, 17748, 17749, 20480, 20481, 20484, 20485, 20496, 20497, 20500, 20501, 20544, 20545, 20548, 20549, 20560, 20561, 20564, 20565, 20736, 20737, 20740, 20741, 20752, 20753, 20756, 20757, 20800, 20801, 20804, 20805, 20816, 20817, 20820, 20821, 21504, 21505, 21508, 21509, 21520, 21521, 21524, 21525, 21568, 21569, 21572, 21573, 21584, 21585, 21588, 21589, 21760, 21761, 21764, 21765, 21776, 21777, 21780, 21781, 21824, 21825, 21828, 21829, 21840, 21841, 21844, 21845};

    // byte8SquaresTable[i] contains the trailing 32 bits of the square of the field element that has i in the last byte (byte 0) of word 1 (and 0s elsewhere).
    // The remaining bits of the square are always 0.
    // The table is all positive (leading bit 0), so conversion to long won't cause a problem -- it will simply prepend 0s
    private static final int [] byte8SquaresTable = {0, 135, 540, 667, 2160, 2295, 2668, 2795, 8640, 8519, 9180, 9051, 10672, 10551, 11180, 11051, 34560, 34695, 34076, 34203, 36720, 36855, 36204, 36331, 42688, 42567, 42204, 42075, 44720, 44599, 44204, 44075, 138240, 138375, 138780, 138907, 136304, 136439, 136812, 136939, 146880, 146759, 147420, 147291, 144816, 144695, 145324, 145195, 170752, 170887, 170268, 170395, 168816, 168951, 168300, 168427, 178880, 178759, 178396, 178267, 176816, 176695, 176300, 176171, 552960, 553095, 553500, 553627, 555120, 555255, 555628, 555755, 545216, 545095, 545756, 545627, 547248, 547127, 547756, 547627, 587520, 587655, 587036, 587163, 589680, 589815, 589164, 589291, 579264, 579143, 578780, 578651, 581296, 581175, 580780, 580651, 683008, 683143, 683548, 683675, 681072, 681207, 681580, 681707, 675264, 675143, 675804, 675675, 673200, 673079, 673708, 673579, 715520, 715655, 715036, 715163, 713584, 713719, 713068, 713195, 707264, 707143, 706780, 706651, 705200, 705079, 704684, 704555, 2211840, 2211975, 2212380, 2212507, 2214000, 2214135, 2214508, 2214635, 2220480, 2220359, 2221020, 2220891, 2222512, 2222391, 2223020, 2222891, 2180864, 2180999, 2180380, 2180507, 2183024, 2183159, 2182508, 2182635, 2188992, 2188871, 2188508, 2188379, 2191024, 2190903, 2190508, 2190379, 2350080, 2350215, 2350620, 2350747, 2348144, 2348279, 2348652, 2348779, 2358720, 2358599, 2359260, 2359131, 2356656, 2356535, 2357164, 2357035, 2317056, 2317191, 2316572, 2316699, 2315120, 2315255, 2314604, 2314731, 2325184, 2325063, 2324700, 2324571, 2323120, 2322999, 2322604, 2322475, 2732032, 2732167, 2732572, 2732699, 2734192, 2734327, 2734700, 2734827, 2724288, 2724167, 2724828, 2724699, 2726320, 2726199, 2726828, 2726699, 2701056, 2701191, 2700572, 2700699, 2703216, 2703351, 2702700, 2702827, 2692800, 2692679, 2692316, 2692187, 2694832, 2694711, 2694316, 2694187, 2862080, 2862215, 2862620, 2862747, 2860144, 2860279, 2860652, 2860779, 2854336, 2854215, 2854876, 2854747, 2852272, 2852151, 2852780, 2852651, 2829056, 2829191, 2828572, 2828699, 2827120, 2827255, 2826604, 2826731, 2820800, 2820679, 2820316, 2820187, 2818736, 2818615, 2818220, 2818091};

    // byte15SquaresTableWord0[i] contains the trailing 16 bits of the square of the field element that has i in the three leading bits (and 0s elsewhere).
    // The remaining bits of word 0 of such a square 0. For the bits of word 1, see byte15SquaresTableWord1.
    // The table is all positive (leading bit 0), so conversion to long won't cause a problem -- it will simply prepend 0s
    private static final short [] byte15SquaresTableWord0 = {0, 270, 1080, 1334, 4199, 4457, 5215, 5457};

    // byte15SquaresTableWord1[i] contains the leading 16 bits of the square of the field element that has i in the eight leading bits (and 0s elsewhere).
    // The remaining bits of word 1 of such a square 0. For the bits of word 0, see byte15SquaresTableWord0.
    // Some of these are negative (leading bit 1), so conversion to long may prepend 0s or 1s, depending on the sign.
    // However, because these represent the UPPER 16 bits of a long this won't be a problem -- just shift left by 48 after conversion.
    private static final short [] byte15SquaresTableWord1 = {0, 135, 540, 667, 2160, 2295, 2668, 2795, 8640, 8519, 9180, 9051, 10672, 10551, 11180, 11051, -30976, -30841, -31460, -31333, -28816, -28681, -29332, -29205, -22848, -22969, -23332, -23461, -20816, -20937, -21332, -21461, 7168, 7303, 7708, 7835, 5232, 5367, 5740, 5867, 15808, 15687, 16348, 16219, 13744, 13623, 14252, 14123, -25856, -25721, -26340, -26213, -27792, -27657, -28308, -28181, -17728, -17849, -18212, -18341, -19792, -19913, -20308, -20437, 28672, 28807, 29212, 29339, 30832, 30967, 31340, 31467, 20928, 20807, 21468, 21339, 22960, 22839, 23468, 23339, -2304, -2169, -2788, -2661, -144, -9, -660, -533, -10560, -10681, -11044, -11173, -8528, -8649, -9044, -9173, 27648, 27783, 28188, 28315, 25712, 25847, 26220, 26347, 19904, 19783, 20444, 20315, 17840, 17719, 18348, 18219, -5376, -5241, -5860, -5733, -7312, -7177, -7828, -7701, -13632, -13753, -14116, -14245, -15696, -15817, -16212, -16341, -16384, -16249, -15844, -15717, -14224, -14089, -13716, -13589, -7744, -7865, -7204, -7333, -5712, -5833, -5204, -5333, 18176, 18311, 17692, 17819, 20336, 20471, 19820, 19947, 26304, 26183, 25820, 25691, 28336, 28215, 27820, 27691, -9216, -9081, -8676, -8549, -11152, -11017, -10644, -10517, -576, -697, -36, -165, -2640, -2761, -2132, -2261, 23296, 23431, 22812, 22939, 21360, 21495, 20844, 20971, 31424, 31303, 30940, 30811, 29360, 29239, 28844, 28715, -20480, -20345, -19940, -19813, -18320, -18185, -17812, -17685, -28224, -28345, -27684, -27813, -26192, -26313, -25684, -25813, 14080, 14215, 13596, 13723, 16240, 16375, 15724, 15851, 5824, 5703, 5340, 5211, 7856, 7735, 7340, 7211, -21504, -21369, -20964, -20837, -23440, -23305, -22932, -22805, -29248, -29369, -28708, -28837, -31312, -31433, -30804, -30933, 11008, 11143, 10524, 10651, 9072, 9207, 8556, 8683, 2752, 2631, 2268, 2139, 688, 567, 172, 43};


    // The tables above are generated by the code below, which is no longer needed.
/*
    public static void genSqrTable256 () {
        byte0SquaresTable = new short[256];
        byte8SquaresTable = new int[256];
        byte15SquaresTableWord0 = new short[8];
        byte15SquaresTableWord1 = new short[256];

        GF2_192 t = new GF2_192();
        GF2_192 res = new GF2_192();

        for (t.word[0] = 0l; t.word[0] < 256l; t.word[0]++) {
            mul(res, t, t);
            byte0SquaresTable[(int) t.word[0]] = (short)res.word[0];
        }

        t.word[0] = 0;
        for (t.word[1] = 0l; t.word[1] < 256l; t.word[1]++) {
            mul(res, t, t);
            byte8SquaresTable[(int) t.word[1]] = (int)res.word[0];
        }

        for (long i = 0; i < 256; i++) {
            t.word[1] = i << 56;
            mul(res, t, t);
            if ((i&31) == 0) {
                byte15SquaresTableWord0[(int) i/32] = (short) res.word[0];
            }
            byte15SquaresTableWord1[(int) i] = (short)(res.word[1]>>>48);
        }

        System.out.println(Arrays.toString(byte0SquaresTable));
        System.out.println(Arrays.toString(byte8SquaresTable));
        System.out.println(Arrays.toString(byte15SquaresTableWord0));
        System.out.println(Arrays.toString(byte15SquaresTableWord1));


        for (short i : byte0SquaresTable) System.out.print("0x"+String.format("%04X", i)+", ");
        System.out.print("};\n");
        for (int i : byte8SquaresTable) System.out.print("0x"+String.format("%06X", i)+", ");
        System.out.print("};\n");
        for (short i : byte15SquaresTableWord0) System.out.print("0x"+String.format("%04X", i)+", ");
        System.out.print("};\n");
        for (short i : byte15SquaresTableWord1) System.out.print("0x"+String.format("%04x", i)+", ");
        System.out.print("};\n");

    }


*/

    /**
     * Squares z and puts the result into res. More efficient that mul(res,z, z)
     * @param res output; must be not null; may be equal to z
     * @param z input to be squared; may be equal to res, in which case will get overwritten
     */

    public static void bettersqr (GF2_192 res, GF2_192 z) {
        // Squaring over finite fields of characteristic 2 is a linear operator
        // Thus, it suffices to precompute squares of every possible byte value
        // in every possible byte position, and then compute res by XORing correct precomputed values
        // together according to the bytes of the input z.
        //
        // Moreover, squares of byte 0 have degree at most 14 (i.e., all bits are 0 except some of the 14 trailing bits).
        // Squares of bytes 1, 2, 3, 4, 5, 6, 7 are simply left shifts (by 2, 4, 6, 8, 10, 12, 14 bytes, respectively)
        // of the squares of the same byte 0, because no modular reduction takes place.
        //
        // For our specific irreducible polynomial, squares of byte 8 have degree less than 22 (i.e., all bits are 0 only
        // except some of the 22 trailing bits), and squares of bytes 9, 10, 11, 12, 13, 14 are simply left shifts
        // (by 2, 4, 6, 8, 10, 12 bytes, respectively) of the squares of the same byte 8.
        //
        // Thus, we need tables only for squares of byte 0, byte 8, and byte 15.
        //
        long input0 = z.word[0];
        long input1 = z.word[1];

        // byte 0 of each input word affects only w0
        long w0;
        w0  = byte0SquaresTable[(int)(input0 & 255)]  ^ byte8SquaresTable[(int)(input1 & 255)];

        int i;
        for (i = 8; i<32; i+=8) {
            // bytes 1 and 2 of each input word affect w0; byte 3 affects both w0 and w1 -- we handle w0 now and w1 below
            w0 ^= ((long)byte0SquaresTable[(int)((input0>>>i) & 255)] ^ byte8SquaresTable[(int)((input1>>>i) & 255)])<<(2*i);
        }

        // byte 3 of input word 1 also affects w1, because the left shift by 6 bytes spills over into word 1
        long w1 = byte8SquaresTable[(int) ((input1>>>24) & 255)]>>>16;

        for (; i<56; i+=8) {
            // byte 4,5,6 of each input word affects only word 1
            w1 ^= ((long)byte0SquaresTable[(int)((input0>>>i) & 255)] ^ byte8SquaresTable[(int)((input1>>>i) & 255)])<<(2*i-64);
        }

        // byte 7 of each input word affect w1
        w1 ^= ((long)byte0SquaresTable[(int)(input0>>>56)]^byte15SquaresTableWord1[(int) (input1>>>56)])<<48;
        // top three bits of input word 1 also affect w0
        w0 ^= byte15SquaresTableWord0[(int) (input1>>>61)];


        res.word[0] = w0;
        res.word[1] = w1;
    }


    // These tables are used in the pow65536 routine below. They take up 8KB.

    // This table of 512 longs contains the result of raising every possible four-bit nibble at offsets 0, 4, 8, 12, ..., 60
    // to the power 65536. The result of raising nibble i at offset shift is contained at indices (shift<<3) | (i<<1) and  (shift<<3) | (i<<1) | 1.
    private static final long [] pow65536Table0 = {0l, 0l, 1l, 0l, 1265483017132502896l, 1153203052884330849l, 1265483017132502897l, 1153203052884330849l, -7798384319384749689l, -8790673725552556346l, -7798384319384749690l, -8790673725552556346l, -9058580678171388169l, -7638033760599568473l, -9058580678171388170l, -7638033760599568473l, 7105175173495992685l, -2683758288860348917l, 7105175173495992684l, -2683758288860348917l, 8292605986949029405l, -3836961195715790998l, 8292605986949029404l, -3836961195715790998l, -1054908926623004438l, 6683377977983727821l, -1054908926623004437l, 6683377977983727821l, -2246218748643530854l, 5530737884181721516l, -2246218748643530853l, 5530737884181721516l, 0l, 0l, -1557807788686251490l, -8401095002407738111l, 3320246870931305730l, 2889165707173743579l, -4291247777759715556l, -6669496263882723622l, -3474274409430310059l, 68101315650465790l, 2713818743693756747l, -8387776931995038977l, -2172134539615578537l, 2948047058134754341l, 845138740121680969l, -6665114464831110876l, -5001206296683293861l, 1562134674829423224l, 5834883149011237189l, -7006332532171319431l, -7742875580272957863l, 4446655726522464675l, 9145203078936153159l, -5270089756640895838l, 8453468207566811150l, 1539141443528039814l, -6975707923429778928l, -7046602450882561913l, 6576137796533454092l, 4414724022689257053l, -5682790207659610350l, -5319577438870850724l, 0l, 0l, -3832504814685449804l, -5254195195637570216l, 2805149652348395574l, -4164882442621421793l, -1423751481466916478l, 8153208790665957959l, -5035048239688903343l, 4149125665157805033l, 8128961458903975141l, -8177975615918795087l, -7137626024259717785l, -24775625744710410l, 6206589068333638867l, 5238429626477535662l, -2292720295241332561l, 8738459521350970817l, 3098100668502432027l, -3580247439087220583l, -4124316808188090215l, -4650419756824208674l, 870125584304683309l, 604391167118655366l, 6499104674181602814l, 4670682985722207784l, -8007047841595798454l, -593137992586249360l, 8997226664625028552l, -8727197546400241353l, -5328703018511192964l, 3600519459750693999l, 0l, 0l, 4454565412537176509l, 5800209625785548884l, -30186365658558855l, 352018473700258515l, -4448138004953194556l, 6096999799569560199l, 7147370653905312645l, -3850379364716477814l, 6836820587702569528l, -7282655182722481442l, -7159511656487234052l, -3570744610612055975l, -6812391866226817983l, -7058177053917695987l, 6324641070245675409l, -2033713755946111198l, 7643849020520140844l, -5496441098186363018l, -6318160902746304536l, -1791215548201771535l, -7673947369950503339l, -5234826405841658459l, 3816004872051153428l, 2978700432736857512l, 658906062853884841l, 8730231929928609276l, -3791488141863922579l, 3293510857393332091l, -670994306594683440l, 9064230101216763695l, 0l, 0l, 6116353354739122873l, 121560905306496990l, 2103383256567545235l, 1204229240181236519l, 5319051417326518058l, 1232181873866341625l, 1530187228019146080l, -4721235023918954521l, 4746222077862035417l, -4623768288211249095l, 580092165671993587l, -5851154620570032960l, 6696078830939095626l, -5808698900301219042l, 3050732107520466336l, 1470366155203677288l, 9130984783405145881l, 1569527308659872694l, 3992114915477594163l, 347201751566790479l, 7171825885805945482l, 395855902602064017l, 4569519322305224896l, -6188781547571541105l, 7749454045575111289l, -6074554323082161071l, 2475438762843118931l, -4995811513422468952l, 8555467686136618986l, -4970688547536504970l, 0l, 0l, -372071414147998392l, -62401654963139720l, -9023442656194909057l, 3248050847003094190l, 8651531102953189687l, -3300809704862636074l, -908565145395990044l, -7039270657567564170l, 698625106867350700l, 7020323088916829454l, 8188224058953555355l, -5522510830935182632l, -8398006714789904173l, 5511939399444265376l, -5418995956140136772l, -7247908971244112235l, 5628928779135794164l, 7226042333532667373l, 3894929051040090819l, -5298163662603281861l, -3685155809777838197l, 5285935405426839875l, 5165623498233861976l, 370770236862523619l, -4793562598177879536l, -430248149563677797l, -4221647988381517017l, 2897600827390477389l, 4593551226572334703l, -2948706982993178827l, 0l, 0l, -4247615643663614439l, -983031944651095638l, 8959373883251071213l, 4662775862049384401l, -5090361702904617228l, -5553483325918812549l, -7245680808434786086l, 6705559872769278787l, 6809261913359818435l, -5812600610117975319l, -1791240083954288585l, 2142427939189220498l, 2461582998210807342l, -1161647797833095880l, -2532893623749691453l, 5483836971902443759l, 1861159519925930458l, -4737486588633423547l, -6877192650087485650l, 913950739479267134l, 7314476549696262455l, -75275875697047916l, 5164345758218786585l, 1230731223268123564l, -9032530862886573824l, -2067154284225158650l, 4322723406571802612l, 5882244786619466877l, -76536889922604563l, -6630846968603277865l, 0l, 0l, -3892712802749991022l, -8649090365951993345l, -3731652745298874684l, -6931729852689429431l, 417937348976826710l, 1744527272868861366l, -8567726991297983924l, -4636479641136796048l, 4675615908231708126l, 4062150621432149903l, 4985432185364358280l, 2335791747965919801l, -8298552482694851814l, -6371990824160216122l, -687661556235581339l, -1166035260420367357l, 4579775037100949495l, 7505577777135732220l, 4198039763992422049l, 8078584131000832074l, -884921348797558477l, -584158071974786635l, 9182204967686668841l, 5797965945991705203l, -5289494734535772741l, -2914179239168693364l, -5522750846647669523l, -3478169640627306950l, 8836468228183776127l, 5207090259903506373l, 0l, 0l, 2414644016726825453l, 1275823829218972994l, -4861908909644595152l, -2989509209396316781l, -7132137841666713123l, -4091658568913182511l, -5548134135961724946l, -8758845536459163034l, -7889294349663597053l, -7510043305560330460l, 1118606833961637854l, 5832571989716996085l, 3315950106949045811l, 4703401032953813687l, -1845489530407974153l, -7304766135272341552l, -4043941110445059302l, -8424859526987339118l, 6549507745111296711l, 5486335219330288195l, 8891811451308675882l, 6744074278862774017l, 6152638829315284249l, 2076746030272742838l, 8421759453750789364l, 965659838364209396l, -1592699828057465559l, -3868252655118684123l, -4006200351751846716l, -2601506396853557913l, 0l, 0l, -1815125113288154238l, 8194244157860984428l, -3687551908891454166l, -9004843466638962837l, 3034386285795456680l, -954882735289960185l, -1139778266997257792l, -8413155290595263020l, 1648828633302346306l, -393578489069876296l, 4394843255563164906l, 591705778166051519l, -2723833399074832536l, 8755530804114588883l, -6967034369050392101l, -647010578758267634l, 8763862440615209561l, -8740757986564228254l, 6017706531110673649l, 8362346533463343717l, -5382837949728030861l, 412862735187112969l, 8034050731763515419l, 8951797719852085466l, -8524804193785289831l, 976402873236637366l, -6652518685425707727l, -57576977312006223l, 4999805733212845747l, -8177200265728095779l, 0l, 0l, -4143999622324367407l, 6155663128708844124l, 9104404284024032730l, -5198713762262794861l, -5177774832014802421l, -2110153712905406513l, -1480524984366144521l, 8445328495415092315l, 3245283414886263846l, 2332471553768975879l, -7697415737042801107l, -4401810251144799800l, 6003517552155632124l, -7528628907470335084l, -5331872543943400124l, 5014122176303582487l, 8105613356831386261l, 1222993990351243083l, -4010396789284509538l, -986348872506806140l, 1019426579275884367l, -6403406266140001576l, 6734425660952643251l, 3505603747252840780l, -7275298867542013598l, 7334999335595254544l, 2534447972785556329l, -8684033647601478433l, -1922573818235763528l, -3309773644266889597l, 0l, 0l, 4593396335224792347l, 4152687406615858604l, -5840340895537297995l, 8664268483925047533l, -7976449677900406610l, 4727906004344599873l, -3578240254771389760l, 2396231184934817666l, -1015406877619601445l, 1792521303699440174l, 6964106077357547381l, 6448228700557555567l, 6852926351124380270l, 6980010867566164675l, -1811273867688421230l, -8982247211434980198l, -2782637830929875575l, -4973685990309805770l, 5201623371727232295l, -331841922172046217l, 8615638171248261180l, -4412330582646081061l, 2921316022539698770l, -6766172106273324264l, 1672415256246576969l, -7225755806581420364l, -8757133373959125017l, -2728037785278940171l, -5059956674126322948l, -2052129432813341095l, 0l, 0l, 2577065727021345740l, 8720609536008050639l, 6025818221142826655l, 1289826217462475553l, 8098477225604265299l, 7558084947994672366l, 6976774937566668782l, 7310260024393673344l, 4832899273805958178l, 2050992610253602127l, 3707160037618694513l, 8400656820610204065l, 1202999819769598653l, 977550456451225198l, 8730224677820046562l, -7528016407982342086l, 6549481293046937390l, -1260224093789988875l, 3064695869977346685l, -8763583855808435429l, 667780930922588593l, -43722348555895596l, 1870549386636983052l, -940022842443978054l, 4194563597900353728l, -8362751163644543627l, 5356336312785380755l, -2084537531222097509l, 7608291575493938783l, -7343145564865997228l, 0l, 0l, -661784758521223193l, -6957429739767435212l, -4089406758740527550l, -2998353763668465317l, 3598196317180088741l, 5265142962156312943l, 1057825010632020758l, 639781338402503479l, -540721699420769039l, -7524728554562794749l, -3922173454080629420l, -2413013921063349652l, 4558058724389651123l, 4751717741107225176l, -9171207846247767622l, 4178618090573301487l, 8532506320777201245l, -6444842931012962597l, 5154036853968476152l, -1180266217841004620l, -5668326096173322209l, 8137130016864280448l, -8207962946670775636l, 3539155028617149912l, 8703113322784724299l, -5877298407622619668l, 5271734978528330990l, -1765921656815561597l, -4613892409435233527l, 8650307811315197111l, 0l, 0l, -4279509991574384312l, -7313459263323893877l, -6339272449689801259l, -3017272618775100402l, 7825638895833906333l, 5521730929752525701l, -6425269771059850960l, 4346708125617423576l, 7082186914078736504l, -6425577225253221549l, 1068121003256560869l, -1552929844543700778l, -3868985487854867027l, 8139056780166877021l, -4544113929402864476l, -1263220805681695378l, 318682599854767596l, 8428905422714288869l, 7563357142948249969l, 4060098232058933600l, -6022912036388646855l, -6712331189260484885l, 7360231284727463316l, -3302616974499073610l, -6721364001347015460l, 5236526177711341117l, -3593122565730332607l, 291258250830771640l, 774208220956541193l, -7022345082944521677l, 0l, 0l, 5404697357896051257l, 3479549138835978840l, 7019419767171770291l, -31177483471517927l, 3055879860615515530l, -3469764303385182911l, -7633650792478538789l, -3308418818131185309l, -2517929387222701598l, -2134718195464467653l, -619757205330008984l, 3280624540830545530l, -4870790853688160687l, 2147875224253074466l, 1059147684704430102l, 2971227868022152746l, 5022514868912046639l, 1833593425323500658l, 8060075692208738213l, -2978339236689536717l, 2655562197807015324l, -1809594107843665045l, -7440779766572241971l, -347394418554097847l, -3189571471786720780l, -3790877376343927535l, -444458678239835010l, 341414456243365968l, -5560361473021172153l, 3815997087347380744l};
    // This table of 512 longs contains the result of raising every possible four-bit nibble at offsets 64, 68, 72, ..., 120
    // to the power 65536. The result of raising nibble i at offset shift is contained at indices ((shift-64)<<3) | (i<<1) and  ((shift-64)<<3) | (i<<1) | 1.
    private static final long [] pow65536Table1 = {0l, 0l, -7516149852976306611l, -8960734616535541869l, 4201776119579830468l, 2726416716411923402l, -5909021118861652343l, -6452744737522631591l, 4886727476448220356l, 8655217597162246152l, -3143405645191417207l, -308336202505091173l, 8763602049252211712l, 6758688092950608834l, -1283574108228019635l, -2418788874695087023l, -3172447252725403052l, 5891293224563082311l, 4920307455622992921l, -3285656651735641132l, -1605954284113552752l, 8364379091420639117l, 9081443303741406429l, -598647660000397282l, -8059173152808950128l, 3017290208957649999l, 547562496191037661l, -6162470053536791588l, -6167779015463317932l, 867432191653855109l, 4455995369579178009l, -8093901276542244842l, 0l, 0l, -8134504110700158369l, -8114385822918414236l, 430214370844458937l, -24173566545082488l, -8438617410450186778l, 8127298930088818668l, 6370201469081514610l, 3525131092324361751l, -2919476186566012883l, -4645426625199548813l, 6746372229924507083l, -3512290622104018529l, -3277632821422264428l, 4621325833078215163l, 4235764997111026735l, 129137734765373761l, -5344617932299880848l, -8166957965921576667l, 4553166034531888022l, -116860145569780023l, -5752090689602680375l, 8143419917384998573l, 7111172468149085789l, 3540113087806066518l, -1318540722100288510l, -4736965416283324622l, 7446587764501633508l, -3563723498029053730l, -1707999495366575173l, 4749315504571644090l, 0l, 0l, -6803271038803967870l, -4634202266145536557l, 2221846108876976149l, -2724064043747681601l, -4665596951707701097l, 7314461215782379372l, -4778803481090696328l, -7869071894491151171l, 2034407143004920826l, 3277337786642694510l, -6666503040196777107l, 5258338367100755458l, 211176357242170351l, -627865716878681135l, -7963723012592162469l, 877575637717611480l, 3525967850151094745l, -5504023322672482805l, -8093327663841339058l, -3017437813228394137l, 3331349084903971276l, 7615562627345116340l, 3230606576123867683l, -6996704132808984731l, -8268460921622072671l, 2402308931393974966l, 3603159884689130038l, 4959756791377882586l, -7812316150662544716l, -331972171683518455l, 0l, 0l, -2341228858178568425l, -5307011731640059811l, 5856533695480450847l, -5322961342013373631l, -8159151846575263736l, 33986103005117212l, -3063069353043398728l, 6971037859797426099l, 792495429662609583l, -2961207956717885458l, -8918432403446194009l, -2981643385956311822l, 6609336163677167536l, 6973445593044427951l, 6938499655394748125l, 6368961100732007985l, -4627228239068784182l, -1280521397316419988l, 3534225032589781442l, -1278438741710371472l, -1256963672223131947l, 6348850954745609517l, -5388757021366482587l, 4097433021214349698l, 7689054997808445042l, -8177211543429275169l, -1985563690736761222l, -8143621642991969597l, 4319959838955649389l, 4081879406703059614l, 0l, 0l, -7490333596901602423l, -5770993013660643240l, -522229748463056215l, 1537663309576457936l, 6975046168614420768l, -4990075471681298808l, -6097535096858409546l, 4942143791259220272l, 3705839507275681343l, -1477367620900001432l, 6026272518047846175l, 5890960332248889312l, -3770247730061818730l, -132375307307735112l, 273196630524851677l, -5317904921855576967l, -7221966599655915948l, 1862891316313072673l, -357407057190338700l, -6672679864777311575l, 7135603327612630269l, 904292694748359409l, -6292757100648763285l, -962358333290361527l, 3505875866569866210l, 6722841114227268881l, 5794734203404073666l, -1732790108326493287l, -4005963649335873205l, 5195664122495931329l, 0l, 0l, -5309170478643051400l, -5210510140406310487l, -7083460326197703069l, -5476547723192756037l, 3161658642580916763l, 310722529945567506l, -4980056281476326973l, 4450207159754616145l, 914583284218535355l, -8470493031776111368l, 2833106588213925792l, -8197348424835795478l, -7997479268589055016l, 4147155188031892547l, 3699272585504864832l, 322401679342521962l, -8861820431147581896l, -5491600724050470973l, -5844534812119763933l, -5222484234867026223l, 1780614112444674139l, 15356742022893432l, -8523838671617711229l, 4159964086699926331l, 4604706832382994427l, -8211287071732303214l, 1443196694741446112l, -8483570485081228416l, -6749406916198700648l, 4464405565203571241l, 0l, 0l, -6193573533507521029l, -4669782175494119505l, -5949970440090863340l, -6126592005355277175l, 531834629511630063l, 1569610975231093542l, -6754501934440594160l, -8172090415045660972l, 598752150784258283l, 3577967839076479355l, 1093887830829431812l, 2625350193387199069l, -6547595591400027649l, -7251209035265866254l, 6193298554287612789l, 7917415279540041764l, -275328588769650l, -3255584773237430389l, -531863117403006367l, -4100045632478704467l, 5949942301031280538l, 8658222932303008514l, -598921027309132187l, -2056271259109435664l, 6754332709083927454l, 6649338305015384415l, 6547461000322686833l, 5300544522099565177l, -1094022072537912694l, -666874750666277418l, 0l, 0l, 990687913682309906l, 5538127450439635585l, -1670834806155475764l, 3829392249676792989l, -1914133263389026338l, 8790985123497891356l, 1821108863207814591l, -7599956644002353139l, 1511592867672336045l, -2712039849504415092l, -1038677554557374093l, -6655409558913010544l, -276336765766131103l, -1191098925821762031l, -5214634166075603821l, -9012081509067160437l, -5035511801333393535l, -3587743758367271414l, 6877530177967767647l, -5203315169849966570l, 5966781505899516749l, -355364386329963881l, -5844540891051331284l, 1470742140638278790l, -6675561226469285314l, 6391243104094676487l, 5058674461316576736l, 2399745532960419867l, 5443625952272967410l, 7896774200548188826l, 0l, 0l, -6904438957689811379l, -3584204653690651130l, 4699212198102772888l, 7010722352093779390l, -2226900541099810091l, -5834046016872535112l, 1742379769318432210l, -7590286767394264082l, -5187957163904298081l, 6407420216051668456l, 6420120635633332554l, -584635948320604592l, -489105745202921721l, 4152508421915689046l, 5769223911255549085l, -866000121309252184l, -1135215184947418416l, 4447587898671090606l, 1235835729675454469l, -7876708131230031850l, -5690276873162111416l, 6697441385268452880l, 5205681505314994511l, 7301103675793432134l, -1724655564273142014l, -6120141448525373376l, 650980321621693911l, 295458331525876728l, -6258245920702325862l, -3865226393232017922l, 0l, 0l, -6795586311141420702l, 8141866661770748704l, -5327057139415088278l, -5005031118645325700l, 1703283700652577288l, -3857555043608792228l, 483001498386758847l, 6380420891641782384l, -6412337464060776995l, 2915648004742103888l, -5719137714869731371l, -2161375660250714100l, 1229677095342239415l, -7855130014455147732l, -1535028901444231947l, -4701994958018568955l, 5405235557475143063l, -3584143027044549083l, 6674341904840253343l, 303331929267015033l, -211320806944715011l, 8415009297977470553l, -1440716383554396086l, -1858607302116229771l, 5598174809584251176l, -7581425045174821291l, 6490797382598599456l, 6682893481141382409l, -314465182057516478l, 3189622354376674857l, 0l, 0l, -8681527484290181374l, 4122403476705235626l, 2940397091859153929l, 8212311678941151364l, -5815441052696538357l, 5242829783138406958l, 6634728987356286976l, 7886766038441860051l, -2623823543786535166l, 6072774192248965497l, 8420932301399036937l, 2054932372895652695l, -911928415566216437l, 2716002653220669949l, 8867971772291537346l, 1799621748682239740l, -246473225377739072l, 2435376942059125846l, 6043618240171318731l, 7570119432682466936l, -3145137088929969463l, 5781442810977205458l, 2810835004240366018l, 8469830122368972079l, -6879508788187177280l, 5530237281885841285l, 1138412453372120523l, 323445986068329899l, -8626239745800299831l, 4415960269914187521l, 0l, 0l, 3948640495657062655l, 2431917956628772813l, -7654870305132345057l, -1518915629014114793l, -6699082868104488480l, -3795318188328916518l, 8040843234756707169l, -401867667395443986l, 6438656314474910622l, -2606547329986169565l, -409088341642818946l, 1191199026897034489l, -3702306553249080703l, 3546608118441798452l, 4834930064811706834l, 4733246405766724749l, 8490829953249171757l, 6922093066549381952l, -2964090638568275763l, -6105644353331551590l, -2301043808742191054l, -8431991187949365929l, 3211012756437437107l, -4916917063027474845l, 1892545981257292364l, -7314846881732834898l, -5094768456116141140l, 5847963368331386996l, -8104266522638576813l, 8113177331757209529l, 0l, 0l, 7642967683722109820l, 3813847257854965934l, -2681825718964906863l, 3306371949114663583l, -5703392030259087379l, 1805686279917099569l, -8597312452526248222l, 8731947646258574233l, -2116283619257709154l, 5603495813843389239l, 5942508952489458291l, 6110412570483970310l, 4064866954626570511l, 6926828714685475240l, 7026110394948056740l, 641083806156757049l, 833311825088288216l, 4325733335930265751l, -4951145278224080331l, 2668103048725871270l, -3361733681209015991l, 1290985298301010440l, -1643259768654030778l, 8199799613863390112l, -8997996963481486534l, 4982682760326714126l, 3745246966867070167l, 6640871599782086975l, 6478582926705651627l, 7549330773526393233l, 0l, 0l, -4812892200862477618l, -671981730131540374l, 1290703843984844846l, 5009765024409955304l, -5990723988539917600l, -5536359276893393534l, 2882272516027258605l, -2907460853318825569l, -7292788368541940701l, 2380781251245559797l, 3897412323364552387l, -7917084615176461705l, -8420799119789413363l, 7245299285309744157l, 693576332714829905l, -8709097205547024511l, -5434367054099645793l, 8182959256831572459l, 1750092319616876671l, -4421038927886674839l, -6522127353886321999l, 3749797175523086851l, 3341653790611275452l, 5802199799324636702l, -7824199089559376782l, -6473596995144290188l, 4590980170831779474l, 1514281802816544246l, -9042282269291428772l, -2040293994889290852l, 0l, 0l, 3283242670198149775l, -8408975391294782986l, 4255442419155046722l, 4239879423876489464l, 1629798961966302157l, -5649129457114750706l, -700838462747642349l, 1444340210671530418l, -2605766752957750116l, -6969880436436881340l, -3654599127294843055l, 3376644308035096906l, -2244997526997191202l, -6516407993538883396l, 7833928250160575508l, -4690417545425921977l, 4694967345526285979l, 3865526060741253553l, 6321341291525989718l, -8917310018923677505l, 8802709580489078745l, 1112961573663160649l, -7281850980159127033l, -6133029873464940043l, -5232689303702089592l, 2426957222289139715l, -6773425624023002299l, -8055802785630385907l, -8327262809859276342l, 1979713993731311867l, 0l, 0l, -1867920449891456127l, 8361087994085011047l, -677788967207156063l, -4674995572199555208l, 1192278222062513440l, -3812409850684698337l, 2839393028491067334l, 4400576989148284218l, -4506875256330906553l, 5267364726404901725l, -3314774202319472281l, -9075059742103799230l, 4029699480178021094l, -718777121047309275l, -6146058693146274302l, 1366052557422390714l, 5523383544528289155l, 7421295205658428381l, 6641915169639288995l, -5914873534090734910l, -5026295810197226718l, -2746447139997352795l, -8227130093588272700l, 3451108497878280320l, 7764360770358772293l, 6623741580914558695l, 8884298329128628069l, -7999562065992181768l, -7108775343312943900l, -1949120117583316577l};

    // This method was used to generate the two above tables; it is no longer needed
/*    public static void genPow65536Table () {
        pow65536Table0 = new long[(64/4)*16*2];
        pow65536Table1 = new long[(64/4)*16*2];

        GF2_192 t = new GF2_192();
        GF2_192 res = new GF2_192();


        for (int shift = 0; shift < 64; shift+=4) {
            for (int i = 0; i<16; i++) {
                t.word[0] = ((long)i)<<shift;
                sqr256(res, t);
                for (int j = 1; j<16; j++) {
                    sqr256(res, res);
                }
                pow65536Table0[(shift<<3)|(i<<1)] = res.word[0];
                pow65536Table0[(shift<<3)|(i<<1)|1] = res.word[1];
            }
        }

        t.word[0] = 0l;
        for (int shift = 0; shift < 64; shift+=4) {
            for (int i = 0; i<16; i++) {
                t.word[1] = ((long)i)<<shift;
                sqr256(res, t);
                for (int j = 1; j<16; j++) {
                    sqr256(res, res);
                }
                pow65536Table1[(shift<<3)|(i<<1)] = res.word[0];
                pow65536Table1[(shift<<3)|(i<<1)|1] = res.word[1];
            }
        }

        System.out.println(Arrays.toString(pow65536Table0));
        System.out.println(Arrays.toString(pow65536Table1));
    }
*/



    /**
     * Raises z to power 2^16=65536 and puts the result into res. More efficient that 16 times sqr.
     * @param res output; must be not null; may be equal to z
     * @param z input to be raised to 2^16; may be equal to res, in which case will get overwritten
     */
    public static void betterpow65536 (GF2_192 res, GF2_192 z) {
        // Squaring over finite fields of characteristic 2 is a linear operator, and
        // therefore is so is raising to the power 65536 = 2^16.
        // Thus, it suffices to precompute squares of every possible four-bit nibble
        // in every possible  position, and then compute res by XORing correct precomputed values
        // together according to the nibbles of the input z.
        //
        // To save on indexing into precompute tables, we store
        // precomputed values as follows: first four bits indicate the nibble number, next four bits
        // indicate the nibble value, and the last bit indicates which word of the result is being stored.
        // Thus, two words of the result are stored next to each other.

        long w0, w1, z0 = z.word[0], z1 = z.word[1];
        int shift;


        int index = ((int)z0&15)<<1;
        w0 = pow65536Table0[index];
        w1 = pow65536Table0[index|1];
        z0>>>=3;
        for (shift = 32; shift < 512; shift+= 32) {
            index = shift | ((int) z0 & 30);
            w0 ^= pow65536Table0[index];
            w1 ^= pow65536Table0[index|1];
            z0>>>=4;
        }

        index = (int)(z1&15)<<1;
        w0 ^= pow65536Table1[index];
        w1 ^= pow65536Table1[index|1];
        z1>>>=3;
        for (shift = 32; shift < 512; shift+= 32) {
            index = shift | ((int) z1 & 30);
            w0 ^= pow65536Table1[index];
            w1 ^= pow65536Table1[index|1];
            z1>>>=4;
        }
        res.word[0] = w0;
        res.word[1] = w1;
    }

    /**
     *
     * @return bits of this in hexadecimal notation, most significant on the left
     */
    public String toString() {
        return String.format("%016X", word[2])+String.format("%016X", word[1])+String.format("%016X", word[0]);
    }
}
