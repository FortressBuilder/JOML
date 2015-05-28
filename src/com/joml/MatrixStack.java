/*
 * (C) Copyright 2015 Kai Burjack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 *  1) The above copyright notice and this permission notice shall be included
 *     in all copies or substantial portions of the Software.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.joml;

/**
 * Resembles the matrix stack known from legacy OpenGL.
 * <p>
 * Their names and semantics resemble those of the legacy OpenGL matrix stack.
 * <p>
 * As with the OpenGL version there is no way to get a hold of any matrix
 * instance within the stack. You can only load the current matrix into a
 * user-supplied {@link Matrix4f} instance.
 * 
 * @author Kai Burjack
 */
public class MatrixStack {

    /**
     * The matrix stack as a non-growable array. The size of the stack must be
     * specified in the {@link #MatrixStack(int) constructor}.
     */
    private Matrix4f[] mats;

    /**
     * The index of the "current" matrix within {@link #mats}.
     */
    private int curr;

    /**
     * Create a new {@link MatrixStack} of the given size.
     * <p>
     * Initially the stack pointer is at zero and the current matrix is set to
     * identity.
     * 
     * @param stackSize
     *            the size of the stack. This must be at least 1
     */
    public MatrixStack(int stackSize) {
        if (stackSize < 1) {
            throw new IllegalArgumentException("stackSize must be >= 1");
        }
        mats = new Matrix4f[stackSize];
        mats[0] = new Matrix4f();
    }

    /**
     * Dispose of all but the first matrix in this stack and set the stack
     * counter to zero.
     * <p>
     * The first matrix will also be set to identity.
     */
    public void clear() {
        curr = 0;
        mats[0].identity();
    }

    /**
     * Load the given {@link Matrix4f} into the current matrix of the stack.
     * 
     * @param mat
     */
    public void loadMatrix(Matrix4f mat) {
        if (mat == null) {
            throw new IllegalArgumentException("mat must not be null");
        }
        mats[curr].set(mat);
    }

    /**
     * Increment the stack pointer by one and set the values of the new current
     * matrix to the one directly below it.
     */
    public void pushMatrix() {
        if (curr == mats.length - 1) {
            throw new IllegalStateException("max stack size of " + (curr + 1)
                    + " reached");
        }
        if (mats[curr + 1] == null) {
            mats[curr + 1] = new Matrix4f(mats[curr]);
        } else {
            mats[curr + 1].set(mats[curr]);
        }
        curr++;
    }

    /**
     * Decrement the stack pointer by one.
     * <p>
     * This will effectively dispose of the current matrix.
     */
    public void popMatrix() {
        if (curr == 0) {
            throw new IllegalStateException(
                    "already at the buttom of the stack");
        }
        curr--;
    }

    /**
     * Stores the current matrix of the stack into the supplied
     * <code>dest</code> matrix.
     * 
     * @param dest
     *            the destination {@link Matrix4f} into which to store the
     *            current stack matrix
     * @return returns <code>dest</code>
     */
    public Matrix4f get(Matrix4f dest) {
        dest.set(mats[curr]);
        return dest;
    }

    /**
     * Apply a translation to the current matrix.
     * <p>
     * If <code>C</code> is the current matrix and <code>T</code> the
     * translation matrix, then the new current matrix will be
     * <code>C * T</code>. So when transforming a vector <code>v</code> with the
     * new matrix by using <code>C * T * v</code>, the translation will be
     * applied first!
     * 
     * @param x
     * @param y
     * @param z
     */
    public void translate(float x, float y, float z) {
        Matrix4f c = mats[curr];
        // translation matrix elements:
        // m00, m11, m22, m33 = 1
        // m30 = x, m31 = y, m32 = z
        // all others = 0
        c.m30 = c.m00 * x + c.m10 * y + c.m20 * z + c.m30;
        c.m31 = c.m01 * x + c.m11 * y + c.m21 * z + c.m31;
        c.m32 = c.m02 * x + c.m12 * y + c.m22 * z + c.m32;
        c.m33 = c.m03 * x + c.m13 * y + c.m23 * z + c.m33;
    }

    /**
     * Apply scaling to the current matrix.
     * <p>
     * If <code>C</code> is the current matrix and <code>S</code> the scaling
     * matrix, then the new current matrix will be <code>C * S</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>C * S * v</code>, the scaling will be applied first!
     * 
     * @param x
     * @param y
     * @param z
     */
    public void scale(float x, float y, float z) {
        Matrix4f c = mats[curr];
        // scale matrix elements:
        // m00 = x, m11 = y, m22 = z
        // m33 = 1
        // all others = 0
        c.m00 = c.m00 * x;
        c.m01 = c.m01 * x;
        c.m02 = c.m02 * x;
        c.m03 = c.m03 * x;
        c.m10 = c.m10 * y;
        c.m11 = c.m11 * y;
        c.m12 = c.m12 * y;
        c.m13 = c.m13 * y;
        c.m20 = c.m20 * z;
        c.m21 = c.m21 * z;
        c.m22 = c.m22 * z;
        c.m23 = c.m23 * z;
    }

    /**
     * Apply rotation to the current matrix.
     * <p>
     * If <code>C</code> is the current matrix and <code>R</code> the rotation
     * matrix, then the new current matrix will be <code>C * R</code>. So when
     * transforming a vector <code>v</code> with the new matrix by using
     * <code>C * R * v</code>, the rotation will be applied first!
     * 
     * @param ang
     *            the angle is in degrees
     * @param x
     * @param y
     * @param z
     */
    public void rotate(float ang, float x, float y, float z) {
        Matrix4f c = mats[curr];
        // rotation matrix elements:
        // m30, m31, m32, m03, m13, m23 = 0
        // m33 = 1
        float cos = (float) Math.cos(TrigMath.degreesToRadians(ang));
        float sin = (float) Math.sin(TrigMath.degreesToRadians(ang));
        float m00 = (cos + x * x * (1.0f - cos));
        float m10 = x * y * (1.0f - cos) - z * sin;
        float m20 = x * z * (1.0f - cos) + y * sin;
        float m01 = y * x * (1.0f - cos) + z * sin;
        float m11 = cos + y * y * (1.0f - cos);
        float m21 = y * z * (1.0f - cos) - x * sin;
        float m02 = z * x * (1.0f - cos) - y * sin;
        float m12 = z * y * (1.0f - cos) + x * sin;
        float m22 = cos + z * z * (1.0f - cos);
        c.set(  c.m00 * m00 + c.m10 * m01 + c.m20 * m02,
                c.m01 * m00 + c.m11 * m01 + c.m21 * m02,
                c.m02 * m00 + c.m12 * m01 + c.m22 * m02,
                c.m03 * m00 + c.m13 * m01 + c.m23 * m02,
                c.m00 * m10 + c.m10 * m11 + c.m20 * m12,
                c.m01 * m10 + c.m11 * m11 + c.m21 * m12,
                c.m02 * m10 + c.m12 * m11 + c.m22 * m12,
                c.m03 * m10 + c.m13 * m11 + c.m23 * m12,
                c.m00 * m20 + c.m10 * m21 + c.m20 * m22,
                c.m01 * m20 + c.m11 * m21 + c.m21 * m22,
                c.m02 * m20 + c.m12 * m21 + c.m22 * m22,
                c.m03 * m20 + c.m13 * m21 + c.m23 * m22,
                c.m30,
                c.m31,
                c.m32,
                c.m33 );
    }

    /**
     * Set the current matrix to identity.
     */
    public void loadIdentity() {
        mats[curr].identity();
    }

    /**
     * Right-multiply the given matrix <code>mat</code> against the current
     * matrix. If <code>C</code> is the current matrix and <code>M</code> the
     * supplied matrix, then the new current matrix will be <code>C * M</code>.
     * So when transforming a vector <code>v</code> with the new matrix by using
     * <code>C * M * v</code>, the supplied matrix <code>mat</code> will be
     * applied first!
     * 
     * @param mat
     */
    public void multMatrix(Matrix4f mat) {
        if (mat == null) {
            throw new IllegalArgumentException("mat must not be null");
        }
        mats[curr].mul(mat);
    }

}