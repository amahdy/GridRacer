package net.amahdy.gridracer;

import java.awt.Point;
import java.util.Random;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class Route {

	static final String ROUTE_EMPTY = "...";
	static final String ROUTE_ME = "/^\\";
	static final String ROUTE_EM = "|_|";

	static final int ROUTE_WIDTH = 3;
	static final int ROUTE_HEIGHT = 11;

	static final Point POINT_INIT = new Point(ROUTE_WIDTH/2, ROUTE_HEIGHT-1);

	Point currentPosition = POINT_INIT;
	IndexedContainer containerData;
	Item[] items = new Item[ROUTE_HEIGHT];

	String[][] routeData = new String[ROUTE_HEIGHT][ROUTE_WIDTH];

	public Route() {
		containerData = new IndexedContainer();
		containerData.addContainerProperty("0", Object.class, ROUTE_EMPTY);
		containerData.addContainerProperty("1", Object.class, ROUTE_EMPTY);
		containerData.addContainerProperty("2", Object.class, ROUTE_EMPTY);
		for(int i=0; i<ROUTE_HEIGHT; i++) {
			items[i] = containerData.getItem(containerData.addItem());
		}

		//init();
	}

	public void init() {
		for(int i=0; i<ROUTE_HEIGHT; i++) {
			for(int j=0; j<ROUTE_WIDTH; j++) {
				routeData[i][j] = ROUTE_EMPTY;
			}
		}
		routeData[POINT_INIT.y][POINT_INIT.x] = ROUTE_ME;

		update();
	}
	
	public IndexedContainer getContainerData() {
		return containerData;
	}
	
	public String[][] getRoute() {
		return routeData;
	}

	boolean updating = false;
	synchronized public void update() {
		
		for(int i=0; i<ROUTE_HEIGHT; i++) {
			items[i].getItemProperty("0").setValue(routeData[i][0]);
			items[i].getItemProperty("1").setValue(routeData[i][1]);
			items[i].getItemProperty("2").setValue(routeData[i][2]);
		}
		updating = false;
	}

	synchronized private void waitUpdate() {
		while(updating) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updating = true;
	}

	synchronized public void moveLeft() {
		waitUpdate();
		if(currentPosition.x>0) {
			routeData[currentPosition.y][currentPosition.x] = ROUTE_EMPTY;
			currentPosition.x--;
			routeData[currentPosition.y][currentPosition.x] = ROUTE_ME;
		}
		update();
	}

	synchronized public void moveRight() {
		waitUpdate();
		if(currentPosition.x<2) {
			routeData[currentPosition.y][currentPosition.x] = ROUTE_EMPTY;
			currentPosition.x++;
			routeData[currentPosition.y][currentPosition.x] = ROUTE_ME;
		}
		update();
	}

	static int lastNum = -2;
	synchronized public boolean rotate() {
		waitUpdate();
		if(routeData[currentPosition.y-1][currentPosition.x].equals(ROUTE_EM)) {
			return false;
		}
		for(int i=ROUTE_HEIGHT-1; i>0; i--) {
			for(int j=0; j<ROUTE_WIDTH; j++) {
				routeData[i][j] = routeData[i-1][j]; 
			}
		}
		routeData[currentPosition.y][currentPosition.x] = ROUTE_ME;
		routeData[0][0] = ROUTE_EMPTY;
		routeData[0][1] = ROUTE_EMPTY;
		routeData[0][2] = ROUTE_EMPTY;
		int randomNum = new Random().nextInt(10);
		if(randomNum<3 && randomNum-1!=lastNum && randomNum+1!=lastNum) {
			routeData[0][randomNum] = ROUTE_EM;
			lastNum = randomNum;
		}else {
			lastNum = -2;
		}
		update();
		return true;
	}
}
