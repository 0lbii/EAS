package edu.asu.stratego.media;

import javafx.scene.image.Image;

public class ImageConstants {

    private static Image load(String path) {
        return new Image(ImageConstants.class.getResource(path).toString());
    }

    public final static Image stratego_logo = load("/images/board/stratego_logo.png");

    // Background for Menu
    public final static Image LOGIN_REGISTER = load("/images/board/login_register.png");
    public final static Image MAIN_MENU = load("/images/board/menu_panel.png");
    
    // Board Images.
    public final static Image SETUP_PANEL = load("/images/board/setup_panel.png");
    public final static Image READY_HOVER = load("/images/board/ready_hover.png");
    public final static Image READY_IDLE = load("/images/board/ready_idle.png");
    public final static Image BORDER = load("/images/board/border.png");
    public final static Image DARK_GRASS = load("/images/board/grass1.png");
    public final static Image LIGHT_GRASS = load("/images/board/grass2.png");

    public final static Image HIGHLIGHT_NONE = load("/images/board/highlight_none.png");
    public final static Image HIGHLIGHT_VALID = load("/images/board/highlight_valid.png");
    public final static Image HIGHLIGHT_INVALID = load("/images/board/highlight_invalid.png");
    public final static Image HIGHLIGHT_WHITE = load("/images/board/highlight_white.png");

    public final static Image MOVEARROW_RED = load("/images/board/movearrow_red.png");
    public final static Image MOVEARROW_BLUE = load("/images/board/movearrow_blue.png");

    public final static Image LAKE_1_1 = load("/images/board/lake1_1.png");
    public final static Image LAKE_1_2 = load("/images/board/lake1_2.png");
    public final static Image LAKE_1_3 = load("/images/board/lake1_3.png");
    public final static Image LAKE_1_4 = load("/images/board/lake1_4.png");

    public final static Image LAKE_2_1 = load("/images/board/lake2_1.png");
    public final static Image LAKE_2_2 = load("/images/board/lake2_2.png");
    public final static Image LAKE_2_3 = load("/images/board/lake2_3.png");
    public final static Image LAKE_2_4 = load("/images/board/lake2_4.png");

    // Piece Images.
    public final static Image RED_02 = load("/images/pieces/red/red_02.png");
    public final static Image RED_03 = load("/images/pieces/red/red_03.png");
    public final static Image RED_04 = load("/images/pieces/red/red_04.png");
    public final static Image RED_05 = load("/images/pieces/red/red_05.png");
    public final static Image RED_06 = load("/images/pieces/red/red_06.png");
    public final static Image RED_07 = load("/images/pieces/red/red_07.png");
    public final static Image RED_08 = load("/images/pieces/red/red_08.png");
    public final static Image RED_09 = load("/images/pieces/red/red_09.png");
    public final static Image RED_10 = load("/images/pieces/red/red_10.png");
    public final static Image RED_SPY = load("/images/pieces/red/red_spy.png");
    public final static Image RED_BACK = load("/images/pieces/red/red_back.png");
    public final static Image RED_BOMB = load("/images/pieces/red/red_bomb.png");
    public final static Image RED_FLAG = load("/images/pieces/red/red_flag.png");

    public final static Image BLUE_02 = load("/images/pieces/blue/blue_02.png");
    public final static Image BLUE_03 = load("/images/pieces/blue/blue_03.png");
    public final static Image BLUE_04 = load("/images/pieces/blue/blue_04.png");
    public final static Image BLUE_05 = load("/images/pieces/blue/blue_05.png");
    public final static Image BLUE_06 = load("/images/pieces/blue/blue_06.png");
    public final static Image BLUE_07 = load("/images/pieces/blue/blue_07.png");
    public final static Image BLUE_08 = load("/images/pieces/blue/blue_08.png");
    public final static Image BLUE_09 = load("/images/pieces/blue/blue_09.png");
    public final static Image BLUE_10 = load("/images/pieces/blue/blue_10.png");
    public final static Image BLUE_SPY = load("/images/pieces/blue/blue_spy.png");
    public final static Image BLUE_BACK = load("/images/pieces/blue/blue_back.png");
    public final static Image BLUE_BOMB = load("/images/pieces/blue/blue_bomb.png");
    public final static Image BLUE_FLAG = load("/images/pieces/blue/blue_flag.png");

}