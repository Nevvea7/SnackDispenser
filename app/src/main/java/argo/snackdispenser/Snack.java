package argo.snackdispenser;

/**
 * Created by Boya on 2/20/2015.
 *
 * Snack object contains name, quantity and id of its icon
 */
public class Snack {
    private String name;
    private int quat;
    private int iconid;
    public Snack(String name, int quat, int iconid) {
        this.name = name;
        this.quat = quat;
        this.iconid = iconid;
    }

    public int getQuat() {
        return this.quat;
    }

    public String getName() {
        return name;
    }

    public int getIconid() {return iconid; }

    public void setQuat(int quat){this.quat = quat;}
}