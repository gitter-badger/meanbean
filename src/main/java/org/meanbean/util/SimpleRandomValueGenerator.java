package org.meanbean.util;

import org.kohsuke.MetaInfServices;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple concrete implementation of RandomValueGenerator.
 * 
 * @author Graham Williamson
 */
@MetaInfServices
public class SimpleRandomValueGenerator implements RandomValueGenerator {

	/**
	 * Generate a random byte.
	 * 
	 * @return A randomly generated byte, which may be positive or negative.
	 */
	@Override
    public byte nextByte() {
		return nextBytes(1)[0];
	}

	/**
	 * Generate a random array of bytes.
	 * 
	 * @param size
	 *            The number of bytes to generate and return. This cannot be a negative number.
	 * 
	 * @return An array of <i>size</i> randomly generated bytes, each of which may be positive or negative.
	 * 
	 * @throws IllegalArgumentException
	 *             If the size parameter is deemed illegal. For example, if it is a negative number.
	 */
	@Override
    public byte[] nextBytes(int size) throws IllegalArgumentException {
		if (size < 0) {
			throw new IllegalArgumentException("Cannot generate a random array of bytes of negative length.");
		}
		byte[] bytes = new byte[size];
		random().nextBytes(bytes);
		return bytes;
	}

	/**
	 * Generate a random int.
	 * 
	 * @return A randomly generated int, which may be positive or negative.
	 */
	@Override
    public int nextInt() {
		return random().nextInt();
	}

    @Override
    public int nextInt(int bound) {
        return random().nextInt(bound);
    }

	/**
	 * Generate a random long.
	 * 
	 * @return A randomly generated long, which may be positive or negative.
	 */
	@Override
    public long nextLong() {
		return random().nextLong();
	}

	/**
	 * Generate a random float between 0.0f (inclusive) and 1.0f (exclusive).
	 * 
	 * @return A randomly generated float.
	 */
	@Override
    public float nextFloat() {
		return random().nextFloat();
	}

	/**
	 * Generate a random double between 0.0d (inclusive) and 1.0d (exclusive).
	 * 
	 * @return A randomly generated double.
	 */
	@Override
    public double nextDouble() {
		return random().nextDouble();
	}

	/**
	 * Generate a random boolean.
	 * 
	 * @return A randomly generated boolean.
	 */
	@Override
    public boolean nextBoolean() {
		return random().nextBoolean();
    }

    private Random random() {
        return ThreadLocalRandom.current();
    }

}