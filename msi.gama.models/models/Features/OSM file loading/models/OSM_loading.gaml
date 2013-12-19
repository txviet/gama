/**
 *  simpleOSMLoading
 *  Author: patricktaillandier
 *  Description: 
 */

model simpleOSMLoading 
 
global{
	
	file osmfile <-  file("../includes/rouen.gz")  ;
	geometry shape <- envelope(osmfile);
	graph the_graph; 
	

	init {
		//possibility to load all of the attibutes of the OSM data: for an exhaustive list, see: http://wiki.openstreetmap.org/wiki/Map_Features
		create osm_agent from:osmfile with: [highway_str::string(read("highway")), building_str::string(read("building"))];
		
		//from the created generic agents, creation of the selected agents
		ask osm_agent {
			if (length(shape.points) = 1 and highway_str != nil ) {
				create node with: [shape ::shape, type:: highway_str];
			} else {
				if (highway_str != nil ) {
					create road with: [shape ::shape, type:: highway_str];
				} else if (building_str != nil){
					create building with: [shape ::shape];
				} 
			}
			do die;
		}
		the_graph <- as_edge_graph(road);
		create people number: 100 {
			target <- any_location_in(one_of (road)) ;
			location <- any_location_in (one_of(road));
		} 
	}
	
	
		
}

species osm_agent frequency: 0{
	string highway_str;
	string building_str;
} 
	
	

species road frequency: 0{
	rgb color <- rgb(rnd(255),rnd(255),rnd(255));
	string type;
	aspect base_ligne {
		draw shape color: color; 
	}
	
} 
	
species node {
	string type;
	aspect base { 
		draw square(3) color: rgb("red") ;
	}
} 
	
species building frequency: 0{
	rgb color <-  rgb(200,200,200);
	aspect base { 
		draw shape color: color;
	}
}  

species people skills: [moving] {
	point target;
	reflex movement {
		do goto on:the_graph target:target speed:1;
	}
	aspect base {
		draw circle(3) color: rgb('green');
	}
		
}

experiment experiment_light type: gui {
	output {
		display carte_principale type: opengl ambient_light: 100{
			species building aspect: base refresh: false;
			species road aspect: base_ligne  refresh: false  ;
			species node aspect: base   refresh: false ;
			species people aspect: base  ;
		}
	}
}