package net.amahdy.gridracer;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Audio;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import elemental.json.JsonArray;

@SuppressWarnings("serial")
@Theme("gridracer")
@Push
public class GridRacerUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = GridRacerUI.class)
	public static class Servlet extends VaadinServlet {
	}

	static final int HIGH_SPEED = 275;
	
	Timer rotater = new Timer();
	int timeVal = HIGH_SPEED;
	int timeIndex = 0;
	final Grid raceGround = new Grid();
	Route route = new Route();
	final Label headMain = new Label("<h1>Vaadin Grid Racer</h1>", ContentMode.HTML);
	final Label head = new Label("<b>Hit 'Space' to begin!</b>", ContentMode.HTML);

	final HorizontalLayout control = new HorizontalLayout();
	final Button out = new Button(".");
	final Button left = new Button("<");
	final Button right = new Button(">");
	final Button start = new Button("Start");

    Audio run = new Audio();
    Audio crash = new Audio();
    Audio win = new Audio();
    Audio turn = new Audio();
    
    final VerticalLayout tweet = new VerticalLayout();
    String tweetState = "I'm Playing Vaadin Grid Racer";
    String tweetHash = "%23vaadin%20%23grid";
    Link iconic = new Link();

	@Override
	protected void init(VaadinRequest request) {

		// <Cleanup when closed
		this.addDetachListener(new DetachListener() {
		    public void detach(DetachEvent event) {
		        System.out.println("Detached.");
		        rotater.cancel();
		    }
		});
		JavaScript.getCurrent().addFunction("aboutToClose", new JavaScriptFunction() {
		
			@Override
			public void call(JsonArray arguments) {
				System.out.println("Window/Tab is Closed.");
				rotater.cancel();
			}
		});
		Page.getCurrent().getJavaScript().execute("window.onbeforeunload = function (e) { var e = e || window.event; aboutToClose(); return; };");
		// Cleanup when closed />
		
		tweet.setVisible(false);
		tweet.addComponent(new Label("<h3>Tweet your Score</h3>", ContentMode.HTML));
        iconic.setIcon(new ThemeResource("tweet.png"));
        iconic.setResource(new ExternalResource("https://twitter.com/intent/tweet?text=" + tweetState + " " + tweetHash));
        iconic.setTargetName("_blank");
		tweet.addComponent(iconic);

		Audio sample = new Audio();
        final Resource audioResource = new ExternalResource(
                "http://mirrors.creativecommons.org/ccmixter/contrib/Wired/The%20Rapture%20-%20Sister%20Saviour%20(Blackstrobe%20Remix).mp3");
        sample.setSource(audioResource);
        //sample.setAutoplay(true);
        sample.setWidth("0px");
        sample.setHeight("0px");

        final Resource runAudio = new ExternalResource("http://www.wavsource.com/snds_2015-01-18_4832380586192403/sfx/motorcycle_hd5.wav");
		run.setSource(runAudio);
		run.setWidth("0px");
		run.setHeight("0px");
	
        turn.setSource(new ThemeResource("turn.wav"));
        turn.setWidth("0px");
        turn.setHeight("0px");
		
        crash.setSource(new ThemeResource("crash.wav"));
        crash.setWidth("0px");
        crash.setHeight("0px");
        
        win.setSource(new ThemeResource("win.wav"));
        win.setWidth("0px");
        win.setHeight("0px");

		left.setWidth("48px");
		left.addFocusListener(new FocusListener() {

			@Override
			public void focus(FocusEvent event) {
				route.moveLeft();
				updateUI();
			}
		});
		left.addShortcutListener(new AbstractField.FocusShortcut(left, KeyCode.ARROW_LEFT));

		out.setWidth("47px");

		right.setWidth("48px");
		right.addFocusListener(new FocusListener() {
			
			@Override
			public void focus(FocusEvent event) {
				route.moveRight();
				updateUI();
			}
		});
		right.addShortcutListener(new AbstractField.FocusShortcut(right, KeyCode.ARROW_RIGHT));

		control.addComponent(left);
		control.addComponent(new Label("&nbsp;", ContentMode.HTML));
		control.addComponent(out);
		control.addComponent(new Label("&nbsp;", ContentMode.HTML));
		control.addComponent(right);
		control.setEnabled(false);

		raceGround.setSelectionMode(SelectionMode.NONE);
		raceGround.setFrozenColumnCount(0);
		raceGround.setHeaderVisible(false);
		raceGround.setContainerDataSource(route.getContainerData());
		raceGround.getColumn("0").setWidth(50.0);
		raceGround.getColumn("1").setWidth(50.0);
		raceGround.getColumn("2").setWidth(50.0);
		raceGround.setWidth("152px");
		raceGround.scrollToEnd();

		start.setWidth("100%");
		start.addClickListener(new ClickListener() {

			int task = 0;

			@Override
			public void buttonClick(ClickEvent event) {
				route.init();
				start.setEnabled(false);
				final Timer starter = new Timer();

				starter.scheduleAtFixedRate(new TimerTask() {
					
					@Override
					public void run() {
		                getUI().access(new Runnable() {

		                    @Override
		                    public void run() {						
		                    	switch (task) {
									case 0:
										head.setValue("<b>Ready...</b>");
										task++;
										break;
									case 1:
										head.setValue("<b>Steady...</b>");
										task++;
										break;
									case 2:
										head.setValue("<b>!!! GO !!!</b>");
										task++;
										break;
									default:
										task = 0;
										starter.cancel();
										timeIndex = 0;
										timeVal = HIGH_SPEED;
										headMain.setValue("<h1>Vaadin Grid Racer</h1>");
										head.setValue("<b>Speed: 0</b>");
										control.setEnabled(true);
										rotate();
										break;
								}
		                    }
		                });
					}
				}, 1337, 1337);
			}
		});
		start.setClickShortcut(KeyCode.SPACEBAR);

		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth("152px");
		//layout.setHeight("500px");
		layout.addComponent(head);
		layout.addComponent(raceGround);
		layout.addComponent(new Label("<hr>", ContentMode.HTML));
		layout.addComponent(control);
		layout.addComponent(new Label("<hr>", ContentMode.HTML));
		layout.addComponent(start);

		HorizontalLayout background = new HorizontalLayout();
		background.addComponent(sample);
		background.addComponent(run);
		background.addComponent(crash);
		background.addComponent(win);
		background.addComponent(turn);
		background.setMargin(true);
		background.setSizeFull();
		VerticalLayout l1 = new VerticalLayout();
		l1.setSizeFull();
		l1.addComponent(headMain);
		l1.addComponent(tweet);
		l1.setExpandRatio(tweet, 1);
		background.addComponent(l1);
		background.setExpandRatio(l1, 1);
		background.addComponent(layout);
		VerticalLayout l2 = new VerticalLayout();
		l2.setSizeFull();
		background.addComponent(l2);
		background.setExpandRatio(l2, 1);
		setContent(background);
	}

	void updateUI() {
		out.focus();
		getUI().access(new Runnable() {
			@Override
			public void run() {
				//turn.
				turn.play();
				//rotater.cancel();
				//rotate();
			}
		});
	}

	public void rotate() {
		if(timeVal==0) {
			getUI().access(new Runnable() {

				@Override
				public void run() {
					win.play();
					headMain.setValue("<h1>You Won Vaadin Grid Racer!</h1>");
					tweetState = "I Won Vaadin Grid Racer!";
					iconic.setResource(new ExternalResource("https://twitter.com/intent/tweet?text=" + tweetState + " " + tweetHash));
					tweet.setVisible(true);
					control.setEnabled(false);
					start.setEnabled(true);
				}
			});
			return;
		}
		rotater = new Timer();
		rotater.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if(!route.rotate()) {
					this.cancel();
					getUI().access(new Runnable() {

						@Override
						public void run() {
							crash.play();
							headMain.setValue("<h1>Game Over!</h1>");
							tweetState = "I Reached Speed " + (HIGH_SPEED - timeVal) + " in Vaadin Grid Racer!";
							iconic.setResource(new ExternalResource("https://twitter.com/intent/tweet?text=" + tweetState + " " + tweetHash));
							tweet.setVisible(true);
							control.setEnabled(false);
							start.setEnabled(true);
						}
					});
					return;
				}
				timeIndex++;
				if(timeIndex==10) {
					rotater.cancel();
					timeIndex=0;
					timeVal-=5;
					rotate();
				}
				getUI().access(new Runnable() {

					@Override
					public void run() {
				        //run.play();
						head.setValue("<b>Speed: " + (HIGH_SPEED - timeVal) + "</b>");
						raceGround.scrollToEnd();
					}
				});
			}
		}, timeVal, timeVal);
	}
}
