/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package codecrafter47.bungeetablistplus.tablist;

/**
 *
 * @author florian
 */
public class TabList {
    private int rows;
    private int collums;
    private int usedSlots;
    private Slot[] slots;
    
    public TabList(int rows, int collums){
        this.rows = rows;
        this.collums = collums;
        this.usedSlots = 0;
        this.slots = new Slot[rows*collums];
    }
    
    public int getRows(){
        return this.rows;
    }
    
    public int getCollums(){
        return this.collums;
    }
    
    public int getUsedSlots(){
        return this.usedSlots;
    }
    
    public Slot getSlot(int n){
        return this.slots[n];
    }
    
    public Slot getSlot(int row, int collum){
        return getSlot(row * collums + collum);
    }
    
    public void setSlot(int n, Slot s){
        if(n >= slots.length)return;
        this.slots[n] = s;
        if(n + 1 > usedSlots)usedSlots = n + 1;
    }
    
    public void setSlot(int row, int collum, Slot s){
        setSlot(row * collums + collum, s);
    }
}
