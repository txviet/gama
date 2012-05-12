model tutorial_gis_city_traffic


global {
	file shape_file_buildings <- file('../includes/building.shp') parameter: 'Shapefile for the buildings:' category: 'GIS' ;
	file shape_file_roads <- file('../includes/road.shp') parameter: 'Shapefile for the roads:' category: 'GIS' ;
	file shape_file_bounds <- file('../includes/bounds.shp') parameter: 'Shapefile for the bounds:' category: 'GIS' ;
	int nb_people <- 100 parameter: 'Number of people agents' category: 'People' ;
	
	init {
		create building from: shape_file_buildings with: [type::read ('NATURE')] {
			if type='Industrial' {
				set color <- rgb('blue') ;
			}
		}
		create road from: shape_file_roads ;
		let residential_buildings type: list of: building <- list(building) where (each.type='Residential');
		create people number: nb_people {
			set location <- any_location_in (one_of (residential_buildings));
		}
	}
}
entities {
	species building {
		string type; 
		rgb color <- rgb('gray')  ;
		aspect base {
			draw geometry: shape  color: color ;
		}
	}
	species road  {
		rgb color <- rgb('black') ;
		aspect base {
			draw geometry: shape color: color ;
		}
	}
	species people {
		rgb color <- rgb('yellow') ;
		aspect base {
			draw shape: circle color: color size: 10 ;
		}
	}
}
environment bounds: shape_file_bounds ;
output {
	display city_display refresh_every: 1 {
		species building aspect: base ;
		species road aspect: base ;
		species people aspect: base ;
	}
}

