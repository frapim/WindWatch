/**
* WindWatch - Wind tracking application compatible with Android Wear.
* Copyright (C) 2014  Francisco Pimenta

* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.frapim.windwatch;

import android.content.Context;
import android.content.res.Resources;

public class Wind {
	public enum Direction {
		NONE("None","", 0),
		NORTH("North", "N", 180),
		SOUTH("South", "S", 0),
		EAST("East", "E", 270),
		WEST("West", "W", 90),
		NORTHEAST("Northeast", "NE", 225),
		SOUTHEAST("Southeast", "SE", 315),
		NORTHWEST("Northwest", "NW", 135),
		SOUTHWEST("Southwest", "SW", 45),
		NORTH_NORTHEAST("North-Northeast", "NNE", 202.5f),
		SOUTH_SOUTHEAST("South-Southeast", "SSE", 337.5f),
		NORTH_NORTHWEST("North-Northwest", "NNW", 157.5f),
		SOUTH_SOUTHWEST("South-Southwest", "SSW", 22.5f),
		EAST_NORTHEAST("East-Northeast", "ENE", 247.5f),
		EAST_SOUTHEAST("East-Southeast", "ESE", 292.5f),
		WEST_NORTHWEST("West-Northwest", "WNW", 112.5f),
		WEST_SOUTHWEST("West-Southwest", "WSW", 67.5f);
		
		public static Direction getForIndex(int index) {
			return values()[index];
		}
		
		Direction(String name, String abbreviation, float degrees) {
			this.name = name;
			this.abbreviation = abbreviation;
			this.degrees = degrees;
		}
		
		public String name;
		public String abbreviation;
		public float degrees;
		
		public static Direction getFromStr(String str) {
			Direction direction = getFromName(str);
			if (direction == Direction.NONE) {
				direction = getFromAbbreviation(str);
			}
			
			return direction;
		}
		
		public static Direction getFromName(String name) {
			for (Direction direction : Direction.values()) {
				if (name.equalsIgnoreCase(direction.name)) {
					return direction;
				}
			}
			
			return Direction.NONE;			
		}
		
		public static Direction getFromAbbreviation(String abbreviation) {
			for (Direction direction : Direction.values()) {
				if (abbreviation.equalsIgnoreCase(direction.abbreviation)) {
					return direction;
				}
			}
			
			return Direction.NONE;
		}
	}
	
	public enum Beaufort {
		CALM(null, 0),
		LIGHT_AIR(1, 3),
		LIGHT_BREEZE(4, 7),
		GENTLE_BREEZE(8, 12),
		MODERATE_BREEZE(13, 17),
		FRESH_BREEZE(18, 24),
		STRONG_BREEZE(25, 30),
		HIGH_WIND(31, 38),
		GALE(39, 46),
		STRONG_GALE(47, 54),
		STORM(55, 63),
		VIOLENT_STORM(64, 73),
		HURRICANE(74, null);
		
		Beaufort(Integer minSpeedMph, Integer maxSpeedMph) {
			if (minSpeedMph != null && maxSpeedMph != null) {
				if (minSpeedMph >= maxSpeedMph) {
					throw new IllegalArgumentException("invalid values " + minSpeedMph + " and " + maxSpeedMph);
				}
			}
			this.minSpeedMph = minSpeedMph;
			this.maxSpeedMph = maxSpeedMph;
		}
		
		public String getText(Context context) {
			Resources res = context.getResources();
			String[] winds = res.getStringArray(R.array.wind_array);
			return winds[ordinal()];
		}
		
		public int getColor(Context context) {
			Resources res = context.getResources();
			int[] colors = res.getIntArray(R.array.wind_colors);
			return colors[ordinal()];
		}
		
		public Integer minSpeedMph;
		public Integer maxSpeedMph;
		
		public static Beaufort getForIndex(int index) {
			return values()[index];
		}
		
		public static Beaufort getForMph(float speedMph) {
			for (Beaufort value: Beaufort.values()) {				
				if (value.maxSpeedMph != null && speedMph <= value.maxSpeedMph) {
					return value;
				}
			}
			
			return HURRICANE;
		}
	}
	
	public void setMph(float mph) {
		this.mph = mph;
		this.scale = Beaufort.getForMph(mph);
	}
		
	public float getMph() {
		return this.mph;
	}
	
	public boolean minMatchesBeaufort(Beaufort beaufort) {
		int min = beaufort.minSpeedMph == null ? 0 :  beaufort.minSpeedMph;
		
		return this.mph >= min;
	}
	
	public boolean matchesDirection(Direction direction) {
		return this.direction == direction;
	}
	
	public static final Wind NULL = new Wind();
	
	public static final Wind TEST = new Wind();
	
	static {
		TEST.scale = Beaufort.FRESH_BREEZE;
		TEST.direction = Direction.NORTH;
		TEST.mph = TEST.scale.maxSpeedMph;
		TEST.text = "Fresh breeze";
	}
	
	
	public Beaufort scale = Beaufort.CALM;
	public Direction direction = Direction.NONE;
	public String text = "";
	public String location = "";
	private float mph = 0.0f;
	public float gustMph = 0.0f;
	public float kph = 0.0f;
	public float gustKph = 0.0f;
	public float degrees = 0.0f;
	public long observationTimestamp = 0;
	
	public Wind() {		
	}
	
	public String getDescriptionText(Context context) {
		return context.getString(R.string.wind_description_format, direction.name, getMph());
	}

	@Override
	public String toString() {
		return "Wind [beaufort=" + scale + ", direction=" + direction
				+ ", text=" + text + ", mph=" + mph + ", gustMph=" + gustMph
				+ ", kph=" + kph + ", gustKph=" + gustKph + ", degrees="
				+ degrees + ", observationTimestamp=" + observationTimestamp
				+ "]";
	}
	
	public static Wind generate(Context context, Beaufort scale) {
		Wind w = new Wind();
		w.scale = scale;
		int min = scale.minSpeedMph == null ? 0 : scale.minSpeedMph;
		int max = scale.maxSpeedMph == null ? 150 : scale.maxSpeedMph;
		if (min != max) {
			w.mph =	min + (int) (Math.random() * ((max - min) + 1));
		} else {
			w.mph = min;
		}
		
		Direction[] dirs = Direction.values();
		int dirOrdinal = 1 + (int)(Math.random() * (dirs.length - 1));
		w.direction = dirs[dirOrdinal];
		w.degrees = w.direction.degrees;
		w.text = scale.getText(context);
		w.location = "Not a real location";
		return w;
	}
}
