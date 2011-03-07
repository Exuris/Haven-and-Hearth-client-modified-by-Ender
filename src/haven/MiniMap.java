/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;
import java.security.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class MiniMap extends Widget {
    static Map<String, Tex> grids = new WeakHashMap<String, Tex>();
    static Set<String> loading = new HashSet<String>();
    static Loader loader = new Loader();
    static Coord mappingStartPoint = null;
    static long mappingSession = 0;
    static Map<String, Coord> gridsHashes = new TreeMap<String, Coord>();
    static Map<Coord, String> coordHashes = new TreeMap<Coord, String>();
    public static final Tex bg = Resource.loadtex("gfx/hud/mmap/ptex");
    public static final Tex nomap = Resource.loadtex("gfx/hud/mmap/nomap");
    public static final Resource plx = Resource.load("gfx/hud/mmap/x");
    public Coord off, doff;
    boolean hidden = false, grid=false;;
    MapView mv;
    boolean dm = false;
    
    static class Loader implements Runnable {
	Thread me = null;
	
	private InputStream getreal(String nm) throws IOException {
	    URL url = new URL(Config.mapurl, nm + ".png");
	    URLConnection c = url.openConnection();
	    c.addRequestProperty("User-Agent", "Haven/1.0");
	    InputStream s = c.getInputStream();
	    /*
	     * I've commented this out, since it seems that the JNLP
	     * PersistenceService (or at least Sun's implementation of
	     * it) is SLOWER THAN SNAILS, so this caused more problems
	     * than it solved.
	     *
	    if(ResCache.global != null) {
		StreamTee tee = new StreamTee(s);
		tee.setncwe();
		tee.attach(ResCache.global.store("mm/" + nm));
		s = tee;
	    }
	    */
	    return(s);
	}
	
	private InputStream getcached(String nm) throws IOException {
	    /*if(ResCache.global == null)
		throw(new FileNotFoundException("No resource cache installed"));
	    return(ResCache.global.fetch("mm/" + nm));*/
	    if (mappingSession > 0) {
		String fileName;
		if (gridsHashes.containsKey(nm)) {
		    Coord coordinates = gridsHashes.get(nm);
		    fileName = "tile_" + coordinates.x + "_"
			    + coordinates.y;
		} else {
		    fileName = nm;
		}
		
		File inputfile = new File("map/"
			+ Utils.sessdate(mappingSession) + "/" + fileName
			+ ".png");
		if(!inputfile.exists())
		    throw(new FileNotFoundException("Minimap cache not found"));
		System.out.print(inputfile.toString()+"\n");
		return new FileInputStream(inputfile);
	    }
	    throw(new FileNotFoundException("No resource cache installed"));
	}

	public void run() {
	    try {
		while(true) {
		    String grid;
		    synchronized(grids) {
			grid = null;
			for(String cg : loading) {
			    grid = cg;
			    break;
			}
		    }
		    if(grid == null)
			break;
		    try {
			InputStream in;
			boolean cached;
			try {
			    in = getcached(grid);
			    cached = true;
			} catch(FileNotFoundException e) {
			    in = getreal(grid);
			    cached = false;
			}
			BufferedImage img;
			try {
			    img = ImageIO.read(in);
			    if ((!cached)&(mappingSession > 0)) {
				String fileName;
				if (gridsHashes.containsKey(grid)) {
				    Coord coordinates = gridsHashes.get(grid);
				    fileName = "tile_" + coordinates.x + "_"
					    + coordinates.y;
				} else {
				    fileName = grid;
				}
				
				File outputfile = new File("map/"
					+ Utils.sessdate(mappingSession) + "/" + fileName
					+ ".png");
				ImageIO.write(img, "png", outputfile);
			    }
			} finally {
			    Utils.readtileof(in);
			    in.close();
			}
			Tex tex = new TexI(img);
			synchronized (grids) {
			    grids.put(grid, tex);
			    loading.remove(grid);
			}
		    } catch(IOException e) {
			synchronized(grids) {
			    grids.put(grid, null);
			    loading.remove(grid);
			}
		    }
		}
	    } finally {
		synchronized(this) {
		    me = null;
		}
	    }
	}
	
	void start() {
	    synchronized(this) {
		if(me == null) {
		    me = new HackThread(this, "Minimap loader");
		    me.setDaemon(true);
		    me.start();
		}
	    }
	}
	
	void req(String nm) {
	    synchronized(grids) {
		if(loading.contains(nm))
		    return;
		loading.add(nm);
		start();
	    }
	}
    }
    
    public static void newMappingSession() {
	long newSession = System.currentTimeMillis();
	String date = Utils.sessdate(newSession);
	try {
	    (new File("map/" + date)).mkdirs();
	    Writer currentSessionFile = new FileWriter("map/currentsession.js");
	    currentSessionFile.write("var currentSession = '" + date + "';\n");
	    currentSessionFile.close();
	    mappingSession = newSession;
	    gridsHashes.clear();
	    coordHashes.clear();
	} catch (IOException ex) {
	}
    }

    public MiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
	super(c, sz, parent);
	this.mv = mv;
	off = new Coord();
	newMappingSession();
    }
    
    public static Tex getgrid(final String nm) {
	return(AccessController.doPrivileged(new PrivilegedAction<Tex>() {
		public Tex run() {
		    synchronized(grids) {
			if(grids.containsKey(nm)) {
			    return(grids.get(nm));
			} else {
			    loader.req(nm);
			    return(null);
			}
		    }
		}
	    }));
    }

    public void draw(GOut g) {
	Coord tc = mv.mc.div(tilesz).add(off);
	Coord ulg = tc.div(cmaps);
	while((ulg.x * cmaps.x) - tc.x + (sz.x / 2) > 0)
	    ulg.x--;
	while((ulg.y * cmaps.y) - tc.y + (sz.y / 2) > 0)
	    ulg.y--;
	boolean missing = false;
	if(!hidden) {
	    for(int y = 0; (y * bg.sz().y) < sz.y; y++) {
		    for(int x = 0; (x * bg.sz().x) < sz.x; x++) {
			g.image(bg, new Coord(x*bg.sz().x, y*bg.sz().y));
		    }
	    }
	}
	outer:
	for(int y = ulg.y; (y * cmaps.y) - tc.y + (sz.y / 2) < sz.y; y++) {
	    for(int x = ulg.x; (x * cmaps.x) - tc.x + (sz.x / 2) < sz.x; x++) {
		Coord cg = new Coord(x, y);
		if (mappingStartPoint == null) {
		    mappingStartPoint = new Coord(cg);
		}
		MCache.Grid grid;
		synchronized(ui.sess.glob.map.req) {
		    synchronized(ui.sess.glob.map.grids) {
			grid = ui.sess.glob.map.grids.get(cg);
			if(grid == null)
			    ui.sess.glob.map.request(cg);
		    }
		}
		Coord relativeCoordinates = cg.sub(mappingStartPoint);
		String mnm = null;
		if(grid == null) {
		    if(coordHashes.containsKey(relativeCoordinates))
			mnm = coordHashes.get(relativeCoordinates);
		    else
			continue;
		}
		if (mnm == null) {
		    if (grid.mnm == null) {
			missing = true;
			break outer;
		    }
		    mnm = grid.mnm;
		}
		if (!gridsHashes.containsKey(mnm)) {
		    if ((Math.abs(relativeCoordinates.x) > 450)
			    || (Math.abs(relativeCoordinates.y) > 450)) {
			newMappingSession();
			mappingStartPoint = cg;
			relativeCoordinates = new Coord(0, 0);
		    }
			gridsHashes.put(mnm, relativeCoordinates);
			coordHashes.put(relativeCoordinates, mnm);
		}
		else {
		    Coord coordinates = gridsHashes.get(mnm);
		    if (!coordinates.equals(relativeCoordinates)) {
			mappingStartPoint = mappingStartPoint.add(relativeCoordinates.sub(coordinates));
		    }
		}
		Tex tex = getgrid(mnm);
		if (tex == null)
		    continue;
		if(!hidden) g.image(tex, cg.mul(cmaps).add(tc.inv()).add(sz.div(2)));
	    }
	}
	
	//grid
	if(grid) {
	    g.chcolor(200,32,64,255);
	    Coord c1, c2;
	    c1 = new Coord();
	    c2 = new Coord(sz.x,0);
	    for(int y = ulg.y+1; (y * cmaps.y) - tc.y + (sz.y / 2) < sz.y; y++) {
		c1.y = (y * cmaps.y) - tc.y + (sz.y / 2);
		c2.y = c1.y;
		g.line(c1, c2, 1);
	    }
	    c1 = new Coord();
	    c2 = new Coord(0,sz.y);
	    for(int x = ulg.x+1; (x * cmaps.x) - tc.x + (sz.x / 2) < sz.x; x++) {
		c1.x = (x * cmaps.x) - tc.x + (sz.x / 2);
		c2.x = c1.x;
		g.line(c1, c2, 1);
	    }
	    g.chcolor();
	}
	//end of grid
	
	if(missing) {
	    if(!hidden) g.image(nomap, Coord.z);
	} else {
	    if((!plx.loading)&&(!hidden)) {
		synchronized(ui.sess.glob.party.memb) {
		    for(Party.Member m : ui.sess.glob.party.memb.values()) {
			Coord ptc = m.getc();
			if(ptc == null)
			    continue;
			ptc = ptc.div(tilesz).add(tc.inv()).add(sz.div(2));
			g.chcolor(m.col.getRed(), m.col.getGreen(), m.col.getBlue(), 128);
			g.image(plx.layer(Resource.imgc).tex(), ptc.add(plx.layer(Resource.negc).cc.inv()));
			g.chcolor();
		    }
		}
	    }
	}
	super.draw(g);
    }
    
    public boolean mousedown(Coord c, int button) {
	if(button == 1) {
	    ui.grabmouse(this);
	    dm = true;
	    doff = c;
	}
	return(true);
    }
    
    public boolean mouseup(Coord c, int button) {
	if(dm) {
	    ui.grabmouse(null);
	    dm = false;
	    return true;
	} else {
	    return super.mouseup(c, button);
	}
    }
    
    public void mousemove(Coord c) {
	if(dm) {
	    off = off.add(doff.sub(c));
	    doff = c;
	} else {
	    super.mousemove(c);
	}
    }
    
    public void hide() {
	hidden = true;
    }
    
    public void show() {
	hidden = false;
    }
}
