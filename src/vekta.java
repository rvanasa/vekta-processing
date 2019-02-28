import java.util.*;
import processing.sound.*;

final String FONTNAME = "font/undefined-medium.ttf";
final int MAX_DISTANCE = 10000; // Maximum distance for updating objects (currently unimplemented)

private Context mainMenu;

Context context; // TODO: convert to stack if necessary

// Settings
JSONObject defaultSettings;
JSONObject  settings;

// Game-balancing variables and visual settings

final float G = 6.674e-11;
final float SCALE = 3e8;
final color UI_COLOR = color(0, 255, 0);
final float VECTOR_SCALE = 5;
final int MAX_PLANETS = 500;
final int TRAIL_LENGTH = 100;
final float DEF_ZOOM = (height/2.0) / tan(PI*30.0 / 180.0); // For some reason, this is the default eyeZ location for Processing

//TEMP
float timeScale = 1;

// Fonts
PFont headerFont;
PFont bodyFont;

// HUD/Menu overlay
PGraphics overlay;

// A sick logo
PShape logo;

// TODO: move all these references to a designated `Resources` class

// Sounds
SoundFile theme;
SoundFile atmosphere;
SoundFile laser;
SoundFile death;
SoundFile engine;
SoundFile change;
SoundFile select;
SoundFile chirp;

// Name components
private String[] planetNamePrefixes;
private String[] planetNameSuffixes;
private String[] itemNameAdjectives;
private String[] itemNameNouns;
private String[] itemNameModifiers;

// Low pass filter
LowPass lowPass;

public void settings() {

}

public void setup() {
  createSettings();
  // Important visual stuff
  fullScreen(P3D);
  pixelDensity(displayDensity());
  background(0);
  frameRate(60);
  noCursor();
  textMode(SHAPE);
  // Overlay initialization
  overlay = createGraphics(width, height);
  // Fonts
  headerFont = createFont(FONTNAME, 72);
  bodyFont = createFont(FONTNAME, 24);
  // Images
  logo = loadShape("VEKTA.svg");
  
  // All sounds and music. These must be instantiated in the main file
  // Music
  theme = new SoundFile(this, "main.wav");
  atmosphere = new SoundFile(this, "atmosphere.wav");
  
  // Sound
  laser = new SoundFile(this, "laser.wav");
  death = new SoundFile(this, "death.wav");
  engine = new SoundFile(this, "engine.wav");
  change = new SoundFile(this, "change.wav");
  select = new SoundFile(this, "select.wav");
  chirp = new SoundFile(this, "chirp.wav");
  
  planetNamePrefixes = loadStrings("data/text/planet_prefixes.txt");
  planetNameSuffixes = concat(loadStrings("data/text/planet_suffixes.txt"), new String[] {""});
  itemNameAdjectives = loadStrings("data/text/item_adjectives.txt");
  itemNameNouns = loadStrings("data/text/item_nouns.txt");
  itemNameModifiers = loadStrings("data/text/item_modifiers.txt");
  
  lowPass = new LowPass(this);
  
  mainMenu = new MainMenu();
  openContext(mainMenu);
}

public void draw() {
  if(context != null) {
    context.render();
  }
  
  hint(DISABLE_DEPTH_TEST);
  camera();
  noLights();
  
  // FPS OVERLAY
  fill(255);
  textAlign(LEFT);
  textSize(16);
  text("FPS = " + frameRate, 50, height - 20);
  //loop();
}

public void keyPressed() {
  if(context != null) {
    context.keyPressed(key);
    if(key == ESC) {
      key = 0; // Suppress default behavior (exit)
    }
    return;
  }
}

public void keyReleased() {
  if(context != null) {
    context.keyReleased(key);
  }
}

public void mousePressed() {
  if(context != null) {
    context.keyPressed('x');
  }
}

public void mouseReleased() {
  if(context != null) {
    context.keyReleased('x');
  }
}

public void mouseWheel(MouseEvent event) {
  if(context != null) {
    context.mouseWheel(event.getCount());
  }
}

void startGamemode(World world) {
  clearOverlay();
  clearContexts();
  openContext(world);
  world.init();
}

void clearOverlay() {
  if(overlay.isLoaded()) {
    overlay.clear();
    overlay.beginDraw();
    overlay.background(0, 0);
    overlay.endDraw();
    overlay.setLoaded(false);
  }
}  

void drawOverlay() {
  // Overlay the overlay
  // NOTE: THIS IS VERY SLOW. Use only for menus, not gameplay!
  if(overlay.isLoaded()) {
    overlay.loadPixels();
    loadPixels();
    for(int i = 0; i < pixels.length; i++)
      if(overlay.pixels[i] != color(0)) pixels[i] = overlay.pixels[i];
    updatePixels();
    overlay.updatePixels();
    //image(overlay, 0, 0);
    //redraw();
  } 
}  

void createSettings() {
  // Default settings
  defaultSettings = new JSONObject();
  defaultSettings.put("sound", 1);
  defaultSettings.put("music", 1);
  // Settings
  try {
    settings = loadJSONObject("settings.json");
  } catch(NullPointerException e) {
    System.out.println("settings.json not found. Using default settings.");
    settings = defaultSettings;
    saveJSONObject(settings, "settings.json");
  }
}

int getSetting(String key) {
  if(!settings.isNull(key)) {
    return settings.getInt(key);
  } else {
    if(!defaultSettings.isNull(key)) {
      return defaultSettings.getInt(key);
    } else {
      return 0;
    }
  }
}

void setSetting(String key, int value) {
  settings.setInt(key, value);
}

void saveSettings() {
  saveJSONObject(settings, "settings.json");
}

  /**
    Draws an option of name "name" at yPos in the overlay
  */
 private void drawOption(String name, int yPos, boolean selected) {
  // Shape ---------------------
  hint(DISABLE_DEPTH_TEST);
  camera();
  noLights();
  if(selected) stroke(255);
  else stroke(UI_COLOR);
  fill(1);
  rectMode(CENTER);
  rect(width / 8, yPos, 200, 50);
  // Text ----------------------
  textFont(bodyFont);
  stroke(0);
  fill(UI_COLOR);
  textAlign(CENTER, CENTER);
  text(name, width / 8, yPos - 3);
}

boolean addObject(Object object) {
  return ((World)context).addObject(object);
}

boolean removeObject(Object object) {
  return ((World)context).removeObject(object);
}

void openContext(Context context) {
  this.context = context;
}

boolean closeContext(Context context) {
  if(this.context == context) {
    this.context = null;
    return true;
  }
  return false;
}

void clearContexts() {
  this.context = null;
}

//// Generator methods (will move to another class) ////

public String generatePlanetName() {
  return random(planetNamePrefixes) + random(planetNameSuffixes);
}

public String generateItemName() {
  String name = random(itemNameNouns);
  if(random(1) > .5) {
    name = random(itemNameAdjectives) + " " + name;
  }
  if(random(1) > .5) {
    name = name + " " + random(itemNameModifiers);
  }
  return name;
}

//// Global utility methods ////

<T> T random(T[] array) {
  return array[(int)random(array.length)];
}

float getDistSq(PVector a, PVector b) {
  float x = a.x - b.x;
  float y = a.y - b.y;
  return x * x + y * y;
}
