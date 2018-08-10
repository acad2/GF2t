package gf2t;

import java.util.*;
import org.junit.*;

import static org.junit.Assert.assertFalse;

public class GF2_128Test {

    private static class GF2t {

        private long[] x;

        public boolean isOne() {
            if (x[0] != 1l) return false;
            for (int i = 1; i < x.length; i++) {
                if (x[i] != 0l) return false;
            }
            return true;
        }

        public boolean equals(long[] that) {
            int i;
            for (i = 0; i < Math.min(x.length, that.length); i++) {
                if (x[i] != that[i])
                    return false;
            }
            for (; i < x.length; i++) {
                if (x[i] != 0) {
                    return false;
                }
            }
            for (; i < that.length; i++) {
                if (that[i] != 0) {
                    return false;
                }
            }
            return true;
        }


        public static void mulBits(GF2t ret, long[] a, long[] b) {
            long[] c = new long[a.length + b.length];


            for (int i = 0; i < a.length; i++) {
                for (int i1 = 0; i1 < 64; i1++) {
                    for (int j = 0; j < b.length; j++) {
                        for (int j1 = 0; j1 < 64; j1++) {
                            if ((a[i] & (1l << i1)) != 0 && (b[j] & (1l << j1)) != 0) {
                                int cPosition = i * 64 + i1 + j * 64 + j1;
                                c[cPosition / 64] ^= 1l << (cPosition % 64);
                            }
                        }
                    }
                }
            }
            ret.x = c;
        }

        private static void modReduce(GF2t poly, Modulus mod) {
            for (int i = poly.x.length * 64 - 1; i >= mod.degree; i--) {
                if ((poly.x[i >> 6] & (1l << (i & 63))) != 0) {
                    for (int j = 0; j < mod.offset.length; j++) {
                        int k = i - mod.offset[j];
                        poly.x[k >> 6] ^= (1l << (k & 63));
                    }
                }
            }
        }

        public String toString() {
            String ret = "";
            for (int i = x.length - 1; i >= 0; i--) {
                ret += x[i];
            }
            return ret;
        }

        public static class Modulus {
            // represented as an array of bit positions
            // where coefficient = 1, counting from degree down
            private final int[] offset;
            private final int degree;

            Modulus(int[] sparseModulus) {
                degree = sparseModulus[0];
                offset = new int[sparseModulus.length];
                offset[0] = 0;
                for (int i = 1; i < sparseModulus.length; i++) {
                    offset[i] = degree - sparseModulus[i];
                }
            }
        }
    }

    private static long[][] testValues = null;
    private static GF2_128 zero = null;
    private static GF2_128 one = null;
    private static int[] pentanomial = {128, 7, 2, 1, 0};
    private static GF2t.Modulus m = new GF2t.Modulus(pentanomial);
    static {genTestValues();}
    private static void genTestValues() {
        if (testValues == null) {
            testValues = new long[1000][];

            for (int i = 0; i < testValues.length; i++) {
                testValues[i] = new long[2];
            }


            // Test single 1s in every bit position but last
            // (1s will be tested separately)
            int j = 0;
            for (int i = 1; i < 64; i++) {
                testValues[j][0] = 1l << i;
                testValues[j++][1] = 0;
            }
            for (int i = 0; i < 64; i++) {
                testValues[j][0] = 0;
                testValues[j++][1] = 1l << i;
            }

            // Test single bytes
            for (int i = 0; i < 256; i++) {
                testValues[j][0] = i;
                testValues[j++][1] = 0;
            }

            // Test first half zero, second half random,
            // and first half random, second half 0
            // and first half random, second half 1

            Random rand = new Random();

            for (int i = 0; i < 100; i++) {
                testValues[j][0] = rand.nextLong();
                testValues[j++][1] = 0;
            }

            for (int i = 0; i < 100; i++) {
                testValues[j][0] = 0;
                testValues[j++][1] = rand.nextLong();
            }

            for (int i = 0; i < 100; i++) {
                testValues[j][0] = rand.nextLong();
                testValues[j++][1] = 1;
            }

            // Test both halves random
            while (j < testValues.length) {
                testValues[j][0] = rand.nextLong();
                testValues[j++][1] = rand.nextLong();
            }
        }
        long[] temp = new long[2];
        zero = new GF2_128(temp);
        temp[0] = 1l;
        one = new GF2_128(temp);
    }



    @Test
    public void constructorAndEqualityTest() {
        GF2_128 t = new GF2_128();
        long[] r = t.toLongArray();
        assertFalse("Fail: empty constructor.", !t.isZero() || r.length != 2 || r[1] != 0l || r[0] != 0l);

        t = new GF2_128(0);
        r = t.toLongArray();
        assertFalse("Fail: constructor on 0 int",!t.isZero() || r.length != 2 || r[1] != 0l || r[0] != 0l );

        t = new GF2_128(1);
        r = t.toLongArray();
        assertFalse("Fail: constructor on 1 int", !t.isOne() || r.length != 2 || r[1] != 0l || r[0] != 1l);

        t = new GF2_128(-1);
        r = t.toLongArray();
        assertFalse("Fail: constructor on 0xFFFFFFFF int " + t, r[0] != 0xFFFFFFFFL || r[1] != 0l);

        long[] s = new long[2];

        s[0] = 123345l;
        s[1] = 123567891234567l;

        t = new GF2_128(s);

        GF2_128 t1 = new GF2_128(t);

        r = t.toLongArray();
        assertFalse("Fail: constructor on long array", r[0] != s[0] || r[1] != s[1]);


        r = t1.toLongArray();
        assertFalse ("Fail: copy constructor",r[0] != s[0] || r[1] != s[1]) ;

        byte[] b = new byte[16];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (r[0] >>> (i * 8));
        }

        for (int i = 0; i < 8; i++) {
            b[i + 8] = (byte) (r[1] >>> (i * 8));
        }

        t = new GF2_128(b);
        s = t.toLongArray();
        assertFalse("Fail: constructor on byte array",r[0] != s[0] || r[1] != s[1] );

        s[0] = 0xFFFFFFFFFFFFFFFFL;
        s[1] = 0xFFFFFFFFFFFFFFFFL;

        t = new GF2_128(s);

        t1 = new GF2_128(t);

        r = t.toLongArray();
        assertFalse("Fail: constructor on long array of all 1s", r[0] != s[0] || r[1] != s[1]);


        r = t1.toLongArray();
        assertFalse("Fail: copy constructor", r[0] != s[0] || r[1] != s[1]);

        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (r[0] >>> (i * 8));
        }

        for (int i = 0; i < 8; i++) {
            b[i + 8] = (byte) (r[1] >>> (i * 8));
        }

        t = new GF2_128(b);
        s = t.toLongArray();
        assertFalse("Fail: constructor on byte array of all 1s", r[0] != s[0] || r[1] != s[1]);

    }

    @Test
    public void sqrTest() {
        GF2_128 res = new GF2_128();
        GF2t res1 = new GF2t();


        // Test squaring
        GF2_128.sqr(res, zero);

        assertFalse("Fail: square of 0", !res.isZero());

        GF2_128 z = new GF2_128(zero);
        GF2_128.sqr(z, z);
        assertFalse("Fail: square of 0 in place", !z.isZero());


        GF2_128.sqr(res, one);
        assertFalse("Fail: square of 1", !res.isOne());

        z = new GF2_128(one);
        GF2_128.sqr(z, z);
        assertFalse("Fail: square of 1 in place", !z.isOne());


        for (long[] p : testValues) {
            z = new GF2_128(p);
            GF2_128.sqr(res, z); // ouptut goes to a different location
            GF2t.mulBits(res1, p, p);
            GF2t.modReduce(res1, m);
            assertFalse("Fail: square " + new GF2_128(p), !res1.equals(res.toLongArray()));
            GF2_128.sqr(z, z);   // output goes to the same location
            assertFalse("Fail: square in place " + new GF2_128(p), !res.equals(z));
        }
    }

    @Test
    public void pow65535Test() {
        GF2_128 res = new GF2_128();


        // Test power 65536
        GF2_128.pow65536(res, zero);
        assertFalse("Fail: pow65536 of 0", !res.isZero());

        GF2_128 z = new GF2_128(zero);
        GF2_128.pow65536(z, z);
        assertFalse("Fail: pow65536 of 0 in place", !z.isZero());


        GF2_128.pow65536(res, one);
        assertFalse("Fail: pow65536 of 1", !res.isOne());

        z = new GF2_128(one);
        GF2_128.pow65536(z, z);
        assertFalse("Fail: pow65536 of 1 in place", !z.isOne());


        GF2_128 res1 = new GF2_128();
        for (long[] p : testValues) {
            z = new GF2_128(p);
            GF2_128.pow65536(res, z);
            GF2_128.sqr(res1, z);
            for (int k = 1; k < 16; k++) {
                GF2_128.sqr(res1, res1);
            }
            assertFalse("Fail: pow65536 " + new GF2_128(p), !res.equals(res1));
            GF2_128.pow65536(z, z); // square into same location
            assertFalse("Fail: pow65536 in place" + new GF2_128(p), !res.equals(z));
        }
    }

    @Test
    public void specialMultTest() {
        GF2_128 res = new GF2_128();
        GF2t res1 = new GF2t();


        // Run everything times 0 and 0 times everything
        // and everything times 1 and 1 times everything
        // where 0 and 1 are GF2_128



        for (long[] p : testValues) {
            GF2_128 p1 = new GF2_128(p);
            GF2_128.mul(res, p1, zero);
            assertFalse("Fail: " + p1 + " * 0", !res.isZero());
            GF2_128.mul(p1, p1, zero);
            assertFalse("Fail: " + p1 + " * 0" + " in place ", !p1.isZero());
            p1 = new GF2_128(p);
            GF2_128.mul(res, zero, p1);
            assertFalse("Fail: 0 * " + p1, !res.isZero());
            GF2_128.mul(p1, zero, p1);
            assertFalse("Fail: 0 * " + p1 + " in place ", !p1.isZero());
            p1 = new GF2_128(p);
            GF2_128.mul(res, p1, one);
            assertFalse("Fail: " + p1 + " * 1", !res.equals(p1));
            GF2_128.mul(p1, p1, one);
            assertFalse("Fail: " + p1 + " * 1 in place", !res.equals(p1));
            GF2_128.mul(res, one, p1);
            assertFalse("Fail: 1 * " + p1, !res.equals(p1));
            GF2_128.mul(p1, one, p1);
            assertFalse("Fail: 1 * " + p1 + " in place", !res.equals(p1));
        }

        // Run everything times 0
        // and everything times 1
        // where 0 and 1 are bytes
        for (long[] p : testValues) {
            GF2_128 p1 = new GF2_128(p);
            GF2_128.mul(res, p1, (byte) 1);
            assertFalse("Fail: " + p1 + " * 1 byte ", !res.equals(p1));
            GF2_128.mul(p1, p1, (byte) 1);
            assertFalse("Fail: " + p1 + " * 1 byte in place", !res.equals(p1));
            GF2_128.mul(res, p1, (byte) 0);
            assertFalse("Fail: " + p1 + " * 0 byte", !res.isZero());
            GF2_128.mul(p1, p1, (byte) 0);
            assertFalse("Fail: " + p1 + " * 0 byte in place", !p1.isZero());
        }


        // Run everything times every byte
        long[] temp = new long[1];
        for (long[] p : testValues) {
            for (int i = 2; i < 256; i++) {
                GF2_128 p1 = new GF2_128(p);
                temp[0] = i;
                GF2_128.mul(res, p1, (byte) i);
                GF2t.mulBits(res1, p, temp);
                GF2t.modReduce(res1, m);
                assertFalse("Fail: " + p1 + " * " + i + " byte", !res1.equals(res.toLongArray()));
                GF2_128.mul(p1, p1, (byte) i);
                assertFalse("Fail: " + p1 + " * " + i + " byte in place", !res.equals(p1));
            }
        }

    }

    @Test
    public void specialAddTest() {
        GF2_128 res = new GF2_128();

        // Run everything plus 0 and 0 plus everything
        // where 0 is GF2_128


        for (long[] p : testValues) {
            GF2_128 p1 = new GF2_128(p);
            GF2_128.add(res, p1, zero);
            assertFalse("Fail: " + p1 + " + 0", !res.equals(p1));
            GF2_128.add(p1, p1, zero);
            assertFalse("Fail: " + p1 + " + 0 in place", !res.equals(p1));
            GF2_128.add(res, zero, p1);
            assertFalse("Fail: 0 + " + p1, !res.equals(p1));
            GF2_128.add(p1, zero, p1);
            assertFalse("Fail: " + p1 + " + 0 in place", !res.equals(p1));
        }
    }

    @Test
    public void generalAddTest() {
        GF2_128 res = new GF2_128();
        GF2t res1 = new GF2t();
        res1.x = new long[2];


        // Try everything plus everything in the test array
        for (long[] p : testValues) {
            GF2_128 p1 = new GF2_128(p);
            for (long[] q : testValues) {
                GF2_128 q1 = new GF2_128(q);
                GF2_128.add(res, p1, q1);
                res1.x[0] = p[0]^q[0];
                res1.x[1] = p[1]^q[1];
                assertFalse("Fail: " + p1 + " + " + q1 + " = " + res + " not " + res1, !res1.equals(res.toLongArray()));
                GF2_128.add(p1, p1, q1);
                assertFalse("Fail: " + p1 + " + " + q1 + " in place 1 ", !res.equals(p1));
                p1 = new GF2_128(p);
                GF2_128.add(q1, p1, q1);
                assertFalse("Fail: " + p1 + " + " + q1 + " in place 2 ", !res.equals(q1));
            }
        }

        // Try everything plus self in the test array, both in place and not, and make sure you get zeros
        for (long[] p : testValues) {
            GF2_128 p1 = new GF2_128(p);
            GF2_128.add(res, p1, p1);
            assertFalse("Fail: " + p1 + " + self", !res.isZero());
            GF2_128.add(p1, p1, p1);
            assertFalse("Fail: " + p1 + " self in place", !p1.isZero());
        }

    }


    @Test
    public void generalMultTest() {
        GF2_128 res = new GF2_128();
        GF2t res1 = new GF2t();

        // Now run everything times everything in the test array
        // TODO: speed this up
        for (long[] p : testValues) {
            GF2_128 p1 = new GF2_128(p);
            for (long[] q : testValues) {
                GF2_128 q1 = new GF2_128(q);
                GF2_128.mul(res, p1, q1);
                GF2t.mulBits(res1, p, q);
                GF2t.modReduce(res1, m);
                assertFalse("Fail: " + p1 + " * " + q1, !res1.equals(res.toLongArray()));
                GF2_128.mul(p1, p1, q1);
                assertFalse("Fail: " + p1 + " * " + q1 + " in place 1 ", !res.equals(p1));
                p1 = new GF2_128(p);
                GF2_128.mul(q1, p1, q1);
                assertFalse("Fail: " + p1 + " * " + q1 + " in place 2 ", !res.equals(q1));
            }
        }
        // Try everything times self in the test array, in place
        for (long [] p: testValues) {
            GF2_128 p1 = new GF2_128(p);
            GF2_128.sqr(res, p1);
            GF2_128.mul(p1, p1, p1);
            assertFalse("Fail: " + p1 + " * self in place", !res.equals(p1));
        }

    }

    @Test
    public void inversionTest() {
        GF2_128 res = new GF2_128(), res2 = new GF2_128();
        GF2t res1 = new GF2t();



        // Test inversion of 1
        GF2_128.invert(res, one);
        assertFalse("Fail: inversion of 1", !res.isOne());

        // Test inversion of everything
        for (long[] p : testValues) {
            GF2_128 p1 = new GF2_128(p);
            if (p1.isZero()) continue;
            GF2_128.invert(res, p1);
            GF2_128.mul(res2, p1, res);
            assertFalse("Fail: inversion of " + p1 + " self-test ", !res2.isOne());
            GF2t.mulBits(res1, res.toLongArray(), p);
            GF2t.modReduce(res1, m);
            assertFalse("Fail: inversion of " + p1 + " gf2t-test", !res1.isOne());
            GF2_128.invert(p1, p1);
            assertFalse("Fail: inversion of " + p1 + " in place ", !p1.equals(res));
        }

    }

    @Test
    public void interpolateTest() {

        // Test for null inputs, arrays of unequal length, etc.
        Optional<GF2_128>[] optArray = new Optional[3];
        optArray[0] = null;
        optArray[1] = Optional.empty();
        optArray[2] = Optional.of(new GF2_128(17));

        GF2_128_Poly res;


        Random rand = new Random();

        for (int len = 1; len < 100; len++) {
            byte[] points = new byte[len];
            GF2_128[] values = new GF2_128[len];
            byte[] temp = new byte[16];
            for (int i = 0; i < len; i++) {
                // generate a byte that is not equal to anything in the array nor 0
                while (true) {
                    byte b;
                    do {
                         b = (byte) rand.nextInt();
                    } while (b==(byte)0);
                    int j;
                    for (j = 0; j < i; j++) {
                        if (b == points[j]) { // detected equality with something in the array
                            break;
                        }
                    }
                    if (j == i) { // did not detect equality with anything in the array
                        points[i] = b;
                        break;
                    }
                }
            }
            for (int i = 0; i < len; i++) {
                rand.nextBytes(temp);
                values[i] = new GF2_128(temp);
            }

            res = GF2_128_Poly.interpolate(points, values, Optional.empty());
            for (int i = 0; i < len; i++) {
                GF2_128 t = res.evaluate(points[i]);
                assertFalse("Interpolation error on length = " + len + " at input point number " + i, !t.equals(values[i]));
            }
            rand.nextBytes(temp);
            GF2_128 valueAt0 = new GF2_128(temp);
            res = GF2_128_Poly.interpolate(points, values, Optional.of(valueAt0));
            for (int i = 0; i < len; i++) {
                GF2_128 t = res.evaluate(points[i]);
                assertFalse("Interpolation error on length =  " + len + " at input point number " + i + "(with optional 0)", !t.equals(values[i]));

            }
            GF2_128 t = res.evaluate((byte) 0);
            assertFalse("Interpolation error on length =  " + len + " at input optional 0", !t.equals(valueAt0));
        }

        for (int len = 1; len < 100; len++) {
            byte[] points = new byte[len];
            GF2_128[] values = new GF2_128[len];
            byte[] temp = new byte[16];

            for (int i = 0; i < len; i++) {
                // generate a byte that is not equal to anything in the array (but may be 0)
                while (true) {
                    byte b = (byte) rand.nextInt();
                    int j;
                    for (j = 0; j < i; j++) {
                        if (b == points[j]) { // detected equality with something in the array
                            break;
                        }
                    }
                    if (j == i) { // did not detect equality with anything in the array
                        points[i] = b;
                        break;
                    }
                }
            }
            for (int i = 0; i < len; i++) {
                rand.nextBytes(temp);
                values[i] = new GF2_128(temp);
            }

            res = GF2_128_Poly.interpolate(points, values, Optional.empty());
            for (int i = 0; i < len; i++) {
                GF2_128 t = res.evaluate(points[i]);
                assertFalse("Interpolation error on length =  " + len + " " + i + "(with 0 allowed but not additional)", !t.equals(values[i]));
            }

            for (Optional<GF2_128> opt : optArray) {
                res = GF2_128_Poly.interpolate(null, values, opt);
                assertFalse("Fail: interpolate should output null on points = null", res != null);
                res = GF2_128_Poly.interpolate(points, null, opt);
                assertFalse("Fail: interpolate should output null on values =  null", res != null);
                res = GF2_128_Poly.interpolate(points, new GF2_128[0], opt);
                assertFalse("Fail: interpolate should output null on values of length 0", res != null);
                res = GF2_128_Poly.interpolate(new byte[0], values, opt);
                assertFalse("Fail: interpolate should output null on points of length 0", res != null);
                res = GF2_128_Poly.interpolate(new byte[len - 1], values, opt);
                assertFalse("Fail: interpolate should output null on not enough points", res != null);
                res = GF2_128_Poly.interpolate(new byte[len + 1], values, opt);
                assertFalse("Fail: interpolate should output null on too many points", res != null);
            }
        }

        for (Optional<GF2_128> opt : optArray) {
            res = GF2_128_Poly.interpolate(null, null, opt);
            assertFalse("Fail: interpolate should output null on both points and values = null", res != null);
        }
    }
}