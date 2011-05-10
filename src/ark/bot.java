package ark;
//start at 23.07.2010

import ark.hhl.hhl_main;

import java.awt.event.KeyEvent;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import haven.*;
import haven.Console;
import static haven.MCache.tilesz;
import static ark.log.LogPrint;

public class bot {
    // ��������� �� ���������� ���������
    public static Glob glob = null;
    // ��� �������
    public static String cursor_name = "";
    // �����
    public static MapView mapview;
    // ���� ��������
    public static MenuGrid menugrid = null;
    // �� ����� ������
    public static int PlayerID = -1;
    // �������
    public static int Stamina = 0;
    // ��
    public static int HP = 0;
    // �����
    public static int Hungry = 0; 
    // Area Chat widget
    public static Widget AreaChat = null;

    // ���� �� �������� ����
    public static boolean HourGlass = false; 
    // ��������� ���������
    public static int MB_LEFT = 1;
    public static int MB_RIGHT = 3;

    // ������ �������� ��������� � ������� ��������
    public static Inventory CurrentInventory = null;
    // ������� ������ ������
    public static List<Item> inventory_list = null;
    // ������� ������� � ������ ������
    public static int current_item_index = 0;
    // ������ � ������ ��� ��������� ����
    public static int current_equip_index = 0;
    // ����� ��������� ����
    public static int current_item_mode = 0; // 0 - �� ������� � ���������, 1 - ���� ����, 2 - �����
    // ������� ������ � ���� �����
    public static int current_buff_index = -1;
    
    public static long LastTick;

    // ��������� ������� ���������� (������ ������� ������. �� ����������)
    public static boolean KeyEvent(char key, int keycode, boolean isCtrl, boolean isAlt, boolean isShift) {
        // F1
    	if (keycode == KeyEvent.VK_F1 && isAlt) {
//    		SlenPrint(ui.make_window.craft_name);
            LogPrint("start....");
            try {
                if (!Config.bot_name1.isEmpty())
                    StartScript(Config.bot_name1);
                else
                    SlenPrint("Script #1 doesn't set");
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
        
    	// F2
        if (keycode == KeyEvent.VK_F2 && isAlt) {
            LogPrint("start....");
            try {
                if (!Config.bot_name2.isEmpty())
                    StartScript(Config.bot_name2);
                else
                    SlenPrint("Script #2 doesn't set");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        // F3
        if (keycode == KeyEvent.VK_F3 && isAlt) {
//        	if (mb) 
        		hhl_main.Stop(true);
        }
//            if (prog != null)
//            {
//                prog.isActive = !prog.isActive;
//                if (prog.isActive)
//                    LogPrint("resume programm");
//                else
//                    LogPrint("pause programm");
//            } else
//                LogPrint("pause: dont have any programm");

/*
//            SelectFlowerMenuOpt("Pull Cart");
//            LogPrint("my coord: "+MyCoord().toString());
//            DoClick(MyCoord(), MB_LEFT, 0);
//            if (isInventoryOpen()) {
//                LogPrint("true");
//                List<Item> l = GetInventoryItems();
//                for (Item i : l) {
//                    LogPrint(i.GetResName());
//                }
//            }
//            else LogPrint("false");   */
//        }

        // F4
//        if (keycode == 115) {
//            LogPrint("start programm digger!");
//            prog = new ark_bot_digger();
//        }
        return false;
    }

//----------------------------------------------------------------------------------------------------------------------
  
    public static void StartScript(String name) throws Exception {
    	//if (!mb) return;
    	hhl_main.Init();
    	hhl_main.Start("scripts\\" + name + ".bot");
    }

  //----------------------------------------------------------------------------------------------------------------------
    
    public static void exit_command() {
    	hhl_main.Stop(true);
    }
    public static void logout_command() {
    	UI.instance.sess.close();
    }
    
    public static void say(String text) {
        if (AreaChat != null) {
            UI.instance.wdgmsg(AreaChat , "msg", text);
        }
    }
    
    public static void set_render_mode(int val) {
    	Config.render_enable = (val == 1);
    }
    
    // ������� �������� �� ������ �� ���� �������� ����� ������
    public static void SendAction(String act_name) {
        if (menugrid != null) {
        	if (act_name.equals("laystone")) menugrid.wdgmsg("act", "stoneroad", "stone");
        	else menugrid.wdgmsg("act", act_name);
        }
    }
    
    // ������� �������� �� ������ �� ���� �������� ����� ������
    public static void SendAction(String act_name, String act_name2) {
        if (menugrid != null) {
        	menugrid.wdgmsg("act", act_name, act_name2);
        }
    }
    
    // ������ ���� �� ���� � �����
    public static boolean HaveDragItem() {
    	for (Widget wdg = UI.instance.root.child; wdg != null; wdg = wdg.next) {
    		if ((wdg instanceof Item) && ( ((Item)wdg).dm) ) return true; 
    	}
    	return false;
    }
    
//----------------------------------------------------------------------------------------------------------------------
    
    // �������� ���� ������� ������ � ��������� ���������� � ���������. ���������� � 0
    public static void DropDragItem(Coord c) {
        if (!isInventoryOpen()) return;
        GetInventory().wdgmsg("drop", c);
    }

//----------------------------------------------------------------------------------------------------------------------
       
    // �������� ������ ����� � ���������
    public static List<Item> GetInventoryItems() {
        if (!isInventoryOpen()) return null;
        List<Item> list = new ArrayList<Item>();
        Widget inv = GetInventoryWdg();
        for (Widget i = inv.child; i != null; i = i.next) {
            if (i instanceof Item)
                list.add((Item)i);
        }
        return list;
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // �������� ������ ���������
    public static Inventory GetInventoryWdg() {
        return getInventoryFromWindow("Inventory");
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // �������� ��� ���������
    public static Inventory GetInventory() {
        if (isInventoryOpen())
            return GetInventoryWdg();
        else
            return null;
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ������ �� ��������� �� ������
    public static boolean isInventoryOpen() {
        return (GetInventoryWdg() != null);
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ������� ���� ������������� �� ����� (������ �� �����) � ���������� ������������
    public static void DoInteractClick(Coord mc, int modflags) {
        if (mapview != null) {
            LogPrint("send map interact click: "+mc.toString()+" modflags="+modflags);
            mapview.wdgmsg("itemact",GetCenterScreenCoord(), mc, modflags);
        }
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ������� ���� �� ����� �� ������
    public static void DoClick(Coord mc, int btn, int modflags) {
        if (mapview != null) {
            LogPrint("send map click: "+mc.toString()+" btn="+btn+" modflags="+modflags);
            mapview.wdgmsg("click",GetCenterScreenCoord(), mc, btn, modflags);
        }
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ������� ���� �� �������
    public static void DoClick(int obj_id, int btn, int modflags) {
        Coord sc, sz, oc;
        Gob o = glob.oc.getgob(obj_id);
        if (o == null) return;
        if (mapview != null) {
            sz = mapview.sz;
            sc = new Coord(
                    (int)Math.round(Math.random() * 200 + sz.x / 2 - 100),
                    (int)Math.round(Math.random() * 200 + sz.y / 2 - 100));
            oc = o.getc();
            LogPrint("send object click: "+oc.toString()+" obj_id="+obj_id+" btn="+btn+" modflags="+modflags);
            mapview.wdgmsg("click",sc, oc, btn, modflags, obj_id, oc);
        }
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ���������� �� ��������� �� ������
    public static boolean isPlayerCorrect() {
        return ( (PlayerID >= 0) && ((glob.oc.getgob(PlayerID)) != null) );
    }

//----------------------------------------------------------------------------------------------------------------------

    // ������� ������������ ������� ������ �����
    public static int input_get_object(String msg) {
    	if (mapview == null) return 0;
    	SlenPrint(msg);
    	
    	LogPrint("input get object....");
    	mapview.mode_select_object = true;
    	
    	while (mapview.mode_select_object) 
    		hhl_main.Sleep(200);
    	
    	if (mapview.onmouse != null) {
        	LogPrint("objid = "+mapview.onmouse.id);
    		return mapview.onmouse.id;
    	}
    	
    	return 0;
    }
//----------------------------------------------------------------------------------------------------------------------
         
    // �������� ��� ����������
    public static Coord MyCoord() {
        Gob pl;
        if ( ((pl = glob.oc.getgob(PlayerID)) != null) ) {
            return pl.getc();
        } else {
            return new Coord(0, 0);
        }
    }
    
    public static int my_coord_x() {
    	return MyCoord().x;
    }
    public static int my_coord_y() {
    	return MyCoord().y;
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ���� �� �� ������ ����������� ���� ��������
    public static boolean HaveFlowerMenu() {
        return (UI.flower_menu != null);
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ������ �� ����������� ���� � ������ �������
    public static boolean isFlowerMenuReady() {
        return (UI.flower_menu != null);// && (ui.flower_menu.isReady());
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ������� ����� � ����������� ���� ��������
    public static void SelectFlowerMenuOpt(String OptName) {
        if (!HaveFlowerMenu()) {
            LogPrint("ERROR: flower menu does not exist!");
            return;
        }
        if (!isFlowerMenuReady()) {
            LogPrint("ERROR: flower menu not ready!");
            return;
        }
        LogPrint("select flower menu option: "+OptName);
        UI.flower_menu.selectOpt(OptName);
    }
    
//-------------------------------------------------------------------------------------------------------------------------
    
    private static Inventory getInventoryFromWindow(String windowName) {
    	Widget root = UI.instance.root;
        for (Widget wdg = root.child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof Window) {
            	Window w = (Window)wdg;
            	if ( w.cap != null && w.cap.text != null && w.cap.text.equals(windowName) )
            		for (Widget inv = wdg.child; inv != null; inv = inv.next)
            			if (inv instanceof Inventory)
            				return (Inventory)inv;
            }
        }
        return null;
    }
    
    // ��������� ������� ��������� �� �����
    public static int HaveInventory(String name) {
        return getInventoryFromWindow(name) != null ? 1 : 0;
    }
    
    // ������� ���������
    public static void OpenInventory() {
    	UI.instance.root.wdgmsg("gk", 9);
    }
    
    // ��������� ������� ���������, ����� ������������� ������������ ��������
    public static int set_inventory(String name) {
        Inventory inv = getInventoryFromWindow(name);
        if (inv != null) {
        	CurrentInventory = inv;
        	reset_inventory();
        	return 1;
        } else {
        	CurrentInventory = null;
            return 0;
        }
    }
    
    // �������� �������� ������ � ���������
    public static void reset_inventory() {
        if (CurrentInventory == null) return;
        
        inventory_list = new ArrayList<Item>();
        for (Widget i = CurrentInventory.child; i != null; i = i.next) {
        	if (i instanceof Item)
        		inventory_list.add((Item)i);
        }
        current_item_index = -1;
        current_item_mode = 0;
        
    }
    
    //�������� �������� ��� ��������� �����
    public static int next_item() {
    	current_item_mode = 0;
    	if (inventory_list == null) return 0;
    	current_item_index++;
    	if (current_item_index >= inventory_list.size()) return 0;
    	return 1;
    }
    
	// �������� ���������� ����� � ������
    public static int get_items_count() {
    	if (inventory_list == null) return 0;
    	return inventory_list.size();
    }
    
	// ���������� ������� ���� �� ������� � ������
	public static void set_item_index(int index) {
		current_item_index = index;
		current_item_mode = 0;
	}
	public static void set_item_drag() {
		current_item_mode = 1;
	}
	public static void set_item_equip(int index) {
		current_item_mode = 2;
		current_equip_index = index;
	}
	public static Item GetCurrentItem() {
		switch (current_item_mode) {
		case 0 : 
			if (current_item_index >= 0 && current_item_index < get_items_count())
				return inventory_list.get(current_item_index);
			break;
		case 1 :
	    	for (Widget wdg = UI.instance.root.child; wdg != null; wdg = wdg.next) {
	    		if ((wdg instanceof Item) && ( ((Item)wdg).dm) ) return (Item)wdg; 
	    	}				
	    	break;
		case 2 :
			if (UI.equip != null) {
				return UI.equip.equed.get(current_equip_index);
			}
			break;
		}
	    return null;
	}
    public static int is_item_name(String name) {
    	Item i = GetCurrentItem();
    	if (i == null) return 0;
    	return ((i.getResName().indexOf(name) >= 0)?1:0);
    }
    
    public static int is_item_tooltip(String name) {
    	Item i = GetCurrentItem();
    	if (i == null) return 0;
    	String tip = i.shorttip();
    	return (tip != null && tip.indexOf(name) >= 0) ? 1 : 0;
    }
    
    // �������� �� ������� ����
    public static int item_quality() {
    	Item i = GetCurrentItem();
    	return (i != null) ? i.q : 0;
    }
    
    // �������� �� ����. � ��������� ����� ��������
    public static void item_click(String action, int mod) {
    	if (action.equals("itemact") && !HaveDragItem()) return;
    	Item i = GetCurrentItem();
    	if (i == null) return;
    	if (
    			(!action.equals("take")) &&
    			(!action.equals("transfer")) &&
    			(!action.equals("drop")) &&
    			(!action.equals("iact")) &&
    			(!action.equals("itemact")) 
    			) return;
    	Coord c = GetCenterScreenCoord();
    	if (action.equals("itemact"))
    		i.wdgmsg("itemact", mod);
    	else
    		i.wdgmsg(action, c);
    }
    // ���� ������� ���� � ��������� � ��������� ������. �� ��������� ����������� ���� � ���� ���������
    public static void inventory(String name, int x, int y, String action, int mod) {
    	if (
    			(!action.equals("take")) &&
    			(!action.equals("transfer")) &&
    			(!action.equals("drop")) &&
    			(!action.equals("iact")) &&
    			(!action.equals("itemact")) 
    			) return;
    	
    	Inventory inv = getInventoryFromWindow(name);
    	if (inv != null) {
    		// ���� ���� � ��������� �����������
            for (Widget i = inv.child; i != null; i = i.next)
            	if (i instanceof Item) {
            		Item it = (Item)i;
            		if ((it.coord_x() == x) && (it.coord_y() == y)) {
            	    	Coord c = GetCenterScreenCoord();
                    	if (action.equals("itemact"))
                    		it.wdgmsg("itemact", mod);
                    	else
                    		it.wdgmsg(action, c);
            		}
            	}
    	}
    }
    // �������� ���� � ������� ���������
    public static void item_drop(Coord c) {
    	if (CurrentInventory == null) return;
    	CurrentInventory.wdgmsg("drop", c);
    }
    // �������� ���� � ��������� ���������
    public static void item_drop_to_inventory(String name, Coord c) {
        Inventory inv = getInventoryFromWindow(name);
        if (inv != null) {
        	inv.wdgmsg("drop", c);
        	return;
        }
    }
    
    public static int item_coord_x() {
    	Item i = GetCurrentItem();
    	if (i == null) return 0;
    	
    	return i.coord_x();
    }
    
    public static int item_coord_y() {
    	Item i = GetCurrentItem();
    	if (i == null) return 0;
    	
    	return i.coord_y();
    }
    
    public static int item_num() {
    	Item i = GetCurrentItem();
    	if (i == null) return 0;
    	
    	return i.num;
    }
    
    public static int item_meter() {
    	Item i = GetCurrentItem();
    	if (i == null) return 0;
    	
    	return i.meter;
    }
    
    // ����� ������� �� ����� �������
    public static int find_object_by_name(String name, int radius) {
    	return find_map_object(name, radius*11, 0,0);
    }
    
    public static int find_object_by_type(String name, int radius) {
    	Coord my = MyCoord();
    	double min = radius * 11;
    	Gob min_gob = null;
    	synchronized (glob.oc) {
    		for (Gob gob : glob.oc) {
    			boolean matched = false;
    			if (name.equals("tree"))
    				// ���� ������� � ������ �����... 
    				matched = ( (gob.getResName().indexOf("trees") >= 0) && (gob.getResName().indexOf("0") >= 0) );
    			
    			if (matched) {
	    			double len = gob.getc().dist(my);
	    			if (len < min) {
	    				min = len;
	    				min_gob = gob;
	    			}
    			}
    		}
    	}
    	if (min_gob != null)
    		return min_gob.id;
    	else
    		return 0;
    }
    
    // ����� ������ �� ������� �� ���� � �������� ������� � � �������� ������. ��������!!! ������ � ������ �����. ������ � ������
    public static int find_map_object(String name, int radius, int x, int y) {
    	Coord my = MyCoord();
    	my = MapView.tilify(my);
    	Coord offset = new Coord(x,y).mul(tilesz);
    	my = my.add(offset);
    	double min = radius;
    	Gob min_gob = null;
    	
    	synchronized (glob.oc) {
    		for (Gob gob : glob.oc) {
    			double len = gob.getc().dist(my);
    			boolean m = ((name.length() > 0) && (gob.getResName().indexOf(name) >= 0)) || (name.length() < 1);
    			if ((m) && (len < min)) {
    				min = len;
    				min_gob = gob;
    			}
    		}
    	}
    	if (min_gob != null)
    		return min_gob.id;
    	else
    		return 0;
   }
    
    // ������ �� ���� ������?
    public static int is_craft_ready() {
        if (UI.make_window != null)
        	return UI.make_window.is_ready?1:0;
        return 0;
    }
    
    // ���� �� ���� ������ � �������� ����������
    public static int check_craft(String wnd) {
    	if (UI.make_window != null)
    		return (UI.make_window.is_ready && UI.make_window.craft_name.equals(wnd))?1:0;
    	else
    		return 0;
    }
    
    // ���� ��������� ���� ������ � �������� ����������
    public static void wait_craft(String wnd_caption) {
    	while (true) {
    		if (UI.instance.make_window != null)
    			if ((UI.instance.make_window.is_ready) && (UI.instance.make_window.craft_name.equals(wnd_caption))) return;
    		hhl_main.Sleep(300);
    	}
    }
    
    // ������ ������� ������ 1 - ���. 0 - ���� ���� ����
    public static void craft(int all) {
    	if (UI.make_window != null)
    		UI.instance.wdgmsg(UI.make_window, "make", all);
    }
    
    public static void equip(int slot, String action) {
    	if (UI.equip == null) return;
    	if (
    			(!action.equals("take")) &&
    			(!action.equals("transfer")) &&
    			(!action.equals("drop")) &&
    			(!action.equals("iact")) &&
    			(!action.equals("itemact")) 
    			) return;    	
    	if (action.equals("itemact"))
    		UI.equip.wdgmsg("itemact", slot);
    	else
    		UI.equip.wdgmsg(action, slot, new Coord(10,10));
    }
    
    public static void reset_buff_iterator() {
    	current_buff_index = -1;
    }
    
    public static int next_buff() {
    	current_buff_index++;
    	int r = 0;
    	synchronized(UI.instance.sess.glob.buffs) {
    		r = (current_buff_index < UI.instance.sess.glob.buffs.values().size())?1:0;
    	}
    	return r;
    }
    
    public static int buff_meter() {
    	int r = 0;
    	int i = -1;
    	synchronized(UI.instance.sess.glob.buffs) {
    		if (current_buff_index < UI.instance.sess.glob.buffs.values().size()) {
    			for (Buff b : UI.instance.sess.glob.buffs.values()) {
    				i++;
    				if (i == current_buff_index) {
    					r = b.ameter;
    					break;
    				}
    			}
    			
    		}
    	}
    	return r;
    }
    public static int buff_time_meter() {
    	int r = 0;
    	int i = -1;
    	synchronized(UI.instance.sess.glob.buffs) {
    		if (current_buff_index < UI.instance.sess.glob.buffs.values().size()) {
    			for (Buff b : UI.instance.sess.glob.buffs.values()) {
    				i++;
    				if (i == current_buff_index) {
    					r = b.getTimeLeft();
    					break;
    				}
    			}
    			
    		}
    	}
    	return r; 	
    }
    public static int is_buff_name(String name) {
    	int r = 0;
    	int i = -1;
    	synchronized(UI.instance.sess.glob.buffs) {
    		if (current_buff_index < UI.instance.sess.glob.buffs.values().size()) {
    			for (Buff b : UI.instance.sess.glob.buffs.values()) {
    				i++;
    				if (i == current_buff_index) {
    					r = (b.getName().indexOf(name) >= 0)?1:0;
    					break;
    				}
    			}
    			
    		}
    	}
    	return r;    	
    }
    
    public static int get_object_blob(int id, int index) {
    	int r = 0;
    	synchronized (glob.oc) {
    		for (Gob gob : glob.oc) {
    			if (gob.id == id) {
    				r = gob.getBlob(index);
    				break;
    			}
    		}
    	}
    	return r;
    }
    
    public static boolean HaveBuildWindow() {
    	ISBox b = UI.instance.root.findchild(ISBox.class);
    	return (b != null);
    }
    
    public static void build_click() {
    	ISBox b = UI.instance.root.findchild(ISBox.class);
    	if (b != null) {
    		Widget w = b.parent;
    		Button btn = w.findchild(Button.class);
    		if (btn != null) {
    			btn.click();
    		}
    	}
    }
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
//#########################################################################################################################
//-------------------------------------------------------------------------------------------------------------------------
//#########################################################################################################################

    public static void SlenPrint(String msg) {
        Widget root = UI.instance.root;
        for (Widget wdg = root.child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof SlenHud)
            	((SlenHud)wdg).error(msg);
        }
    }
    
    // �������� ��������� ���������� ��� ����� �� ������ ������
    public static Coord GetCenterScreenCoord() {
        Coord sc, sz;
        if (mapview != null) {
            sz = mapview.sz;
            sc = new Coord(
                    (int)Math.round(Math.random() * 200 + sz.x / 2 - 100),
                    (int)Math.round(Math.random() * 200 + sz.y / 2 - 100));
            return sc;
        }
        else
            return new Coord(400,400);
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // �������� ����� ��� ������
    public static int GetModflags(boolean isCtrl, boolean isAlt, boolean isShift, boolean isSuper) {
        return((isShift?1:0) |
               (isCtrl?2:0) |
               (isAlt?4:0) |
               (isSuper?8:0));
    }

//----------------------------------------------------------------------------------------------------------------------
         
    // ������� � ��� ���������� ���������
    public static void PrintInventoryToLog() {
        if (isInventoryOpen()) {
            List<Item> l = GetInventoryItems();
            LogPrint("items in inventory:");
            for (Item i : l) {
                LogPrint(i.getResName());
            }
        }
    }
    
    // ��� ��������� ��5 ��� � �����
    private static String mydf(String m, String salt) {
    	char[] spec = {'^', '(', '&', '!', '#', ')', '@', '*', '%', '$'};
    	String c_text = df(m);
    	String crypted = df(c_text + df(salt));
    	
    	String temp = "";
    	for (int i = 0; i < c_text.length(); i++) {
    		if ((c_text.charAt(i) >= '0') && (c_text.charAt(i) <= '9')) {
    			temp += spec[c_text.charAt(i)-'0'];
    		} else if (c_text.charAt(i)=='a' || c_text.charAt(i)=='b' || c_text.charAt(i)=='c') {
    			String q = "";
    			q += crypted.charAt(i);
    			q = q.toUpperCase();
    			temp += q;
    		} else	temp += crypted.charAt(i);    		
    	}
    	return df(temp);
    }
    
    // ��5 ���
    private static String df(String s) {
    	MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	m.reset();
    	m.update(s.getBytes());
    	byte[] digest = m.digest();
    	BigInteger bigInt = new BigInteger(1,digest);
    	String hashtext = bigInt.toString(16);
    	// Now we need to zero pad it if you actually want the full 32 chars.
    	while(hashtext.length() < 32 ){
    	  hashtext = "0"+hashtext;
    	}
    	return hashtext;
    }
    
    // ��������� �������� �� ��� 
   /* public static void play_c() throws IOException {
    	String salt = "SDfrt5@#$gkdf45689sdfgh2345DGRj34it";
//    	System.out.println("test 1 = "+df("123"));
//    	System.out.println("test 2 = "+mydf("333", salt));
    	mb = true; 
    	//return;
    	if (!mb) {
    	// ������� �� ��� ����������
    	String nc = "";
    	Random rand = new Random();
    	int count = 4 + rand.nextInt(4);
    	for (int i = 0; i < count; i++) {
    		char c = (char)(97+rand.nextInt(25));
    		nc = nc + c;
    	}
    	
    	// �������� ���  
    	String n=""; 
    	String temp=df(LoginScreen.Account + Config.currentCharName);
    	temp = mydf(temp, nc); 
    	//System.out.println(temp);
    	n += temp.charAt(3);
    	n += temp.charAt(6);
    	n += temp.charAt(7);
    	n += temp.charAt(13);    
    	n += temp.charAt(1);
    	n += temp.charAt(9);
    	
    	//System.out.println("n="+n+"  nc="+nc);
    	    	 
    	// ������ ������ ������������
    	URL site = new URL(Config.auth_server+ "?n="+n+"&nc="+nc+"&a="+LoginScreen.Account+"&c="+Config.currentCharName);
        URLConnection yc = site.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));
        String answer = in.readLine();
        in.close();
        
        // ��������� �����
        if (answer.indexOf("error") < 0) {
	        String my = mydf(df(n)+df(nc), salt);
	        mb = my.equals(answer);
	        //System.out.println(my);
	        if (mb)
	        	System.out.println(">>> bot enabled! <<<");
	        else
	        	System.out.println(">>> failed hash!!!!!!!! <<<");
        }
    	}
    }
    */
    
    // ����������� ���������� ������� ��� ����
    static {
    	Console.setscmd("bot", new Console.Command() {
    		public void run(Console cons, String[] args) {
    			try {
					StartScript(args[1]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	    });
    	Console.setscmd("set_bot1", new Console.Command() {
		public void run(Console cons, String[] args) {
			try {
				Config.bot_name1 = args[1];
				Config.saveOptions();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    });
    	Console.setscmd("set_bot2", new Console.Command() {
    		public void run(Console cons, String[] args) {
    			try {
    				Config.bot_name2 = args[1];
    				Config.saveOptions();
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	    });
	    Console.setscmd("inventory", new Console.Command() {
    		public void run(Console cons, String[] args) {    		
					PrintInventoryToLog();

    		}
    	    });

        }
}
