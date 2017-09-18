package com.pre.canva.image;

import android.graphics.Rect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.*;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by LeonardWu on 24/11/2016.
 */
//Todo : add more unit tests
@RunWith(RobolectricTestRunner.class)
public class TileSegmentationTest {
    @Test
    public void testSingleTile_1x1() throws Exception {
        ArrayList<Rect> list = BitmapHelper.tileSegmentation(1,1,32,32);

        assertThat(list.size(), equalTo(1));

        final Rect tile = list.get(0);
        assertThat(tile.left, equalTo(0));
        assertThat(tile.top, equalTo(0));
        assertThat(tile.right, equalTo(31));
        assertThat(tile.bottom, equalTo(31));
    }

    @Test
    public void testSingleTile() throws Exception {
        ArrayList<Rect> list = BitmapHelper.tileSegmentation(32,32,32,32);

        assertThat(list.size(), equalTo(1));

        final Rect tile = list.get(0);
        assertThat(tile.left, equalTo(0));
        assertThat(tile.top, equalTo(0));
        assertThat(tile.right, equalTo(31));
        assertThat(tile.bottom, equalTo(31));
    }

    @Test
    public void testTwoTiles() throws Exception {
        ArrayList<Rect> list = BitmapHelper.tileSegmentation(33,32,32,32);

        assertThat(list.size(), equalTo(2));

        final Rect tile1 = list.get(0);
        assertThat(tile1, equalTo(new Rect(0, 0, 31, 31)));

        final Rect tile2 = list.get(1);
        assertThat(tile2, equalTo(new Rect(32, 0, 63, 31)));
    }

    @Test
    public void testFourTiles() throws Exception {
        ArrayList<Rect> list = BitmapHelper.tileSegmentation(33,33,32,32);

        assertThat(list.size(), equalTo(4));

        final Rect tile1 = list.get(0);
        assertThat(tile1, equalTo(new Rect(0, 0, 31, 31)));

        final Rect tile2 = list.get(1);
        assertThat(tile2, equalTo(new Rect(32, 0, 63, 31)));

        final Rect tile3 = list.get(2);
        assertThat(tile3, equalTo(new Rect(0, 32, 31, 63)));

        final Rect tile4 = list.get(3);
        assertThat(tile4, equalTo(new Rect(32, 32, 63, 63)));
    }

    @Test
    public void testTile_1x1() throws Exception {
        ArrayList<Rect> list = BitmapHelper.tileSegmentation(1,1,1,1);

        assertThat(list.size(), equalTo(1));

        final Rect tile = list.get(0);
        assertThat(tile.left, equalTo(0));
        assertThat(tile.top, equalTo(0));
        assertThat(tile.right, equalTo(0));
        assertThat(tile.bottom, equalTo(0));
    }

    @Test
    public void testTile_2x1() throws Exception {
        ArrayList<Rect> list = BitmapHelper.tileSegmentation(1,1,2,1);

        assertThat(list.size(), equalTo(1));

        final Rect tile = list.get(0);
        assertThat(tile.left, equalTo(0));
        assertThat(tile.top, equalTo(0));
        assertThat(tile.right, equalTo(1));
        assertThat(tile.bottom, equalTo(0));
    }
}