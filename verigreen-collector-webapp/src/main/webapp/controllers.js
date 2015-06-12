/*******************************************************************************
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
var app = angular.module('App', ['ngCookies','angularModalService']);
app.controller('ctrlRead', ['$scope','$filter','$http', 'ModalService', 'sharedProperty', '$cookies', '$interval', '$cookieStore',
   function ($scope, $filter, $http, ModalService, sharedProperty, $cookies, $interval, $cookieStore) {

	// init
	$scope.sortingOrder = sortingOrder;
	$scope.reverse = false;
	$scope.filteredItems = [];
	$scope.groupedItems = [];
	$scope.itemsPerPage = 40;
	$scope.pagedItems = [];
	$scope.currentPage = 0;
	$scope.items = [];
	$scope.clicked = [];
	$scope.historyData = [];
	$scope.historyValues = [];
	$scope.check = false;
	$scope.number = /^\d+$/;
	$scope.time = "30";
	$scope.checklist = true;
	$scope.color = "#A8A8A8";
	
	$scope.getValueFilter = function(column) {
		if($cookieStore.get(column) == true) {
			  $scope.colorFilter = "#0088CC";
			  return false;
		  }
		  else {
			  return true;
		  }
	};
	
    $scope.toogleFilter = function(column,show) {
    	$cookieStore.put(column,show);
    	$scope.filterColor();
    };
    
    $scope.filterColor = function() {
    	if($cookieStore.get('Id') == true || $cookieStore.get('ProtectedBranch') == true || $cookieStore.get('ParentBranch') == true || 
    			$cookieStore.get('Committer') == true || $cookieStore.get('Status') == true || $cookieStore.get('Retry') == true || $cookieStore.get('CreationTime') == true ||
    			$cookieStore.get('RunTime') == true || $cookieStore.get('EndTime') == true || $cookieStore.get('BuildUrl') == true) {
    		 $scope.colorFilter = "#0088CC";
    	}
    	else {
    		 $scope.colorFilter = "#A8A8A8";
    	}
    };
    
    $scope.toogle = function() {
  		$scope.check =!$scope.check;
  		if($scope.check == true){
  			$scope.color = "#0088CC";
  			$scope.cols = 450;
  			$cookies.time = $scope.time;
  			$scope.refresh();
			$scope.clicked = [];
			$scope.commitMessage();
			$scope.historyResource();
  			timer = $interval(function() {
  				$scope.refresh();
  				$scope.clicked = [];
  				$scope.commitMessage();
  				$scope.historyResource();
  			},$scope.time*1000);
  		}
  		else if($scope.check == false){
  			$scope.color = "#A8A8A8";
  			$scope.cols = 330;
  			$cookies.time = "0";
  			$interval.cancel(timer);
            timer=undefined;
  		}
  	};
    
  	$scope.changeTime=function(validation){
  		if(validation == true) {
			$cookies.time = $scope.time;
			$interval.cancel(timer);
			timer = $interval(function(){
  				$scope.refresh();
  				$scope.clicked = [];
  				$scope.commitMessage();
  				$scope.historyResource();
  			},$scope.time*1000);
		} 
      };
      
    $scope.autoreload = function() {
  		if($cookies.time > 0) {
  			$scope.color = "#0088CC";
  			$scope.cols = 450;
  			$scope.check = true;
  			$scope.time = $cookies.time;
  			timer = $interval(function(){
  				$scope.refresh();
  				$scope.clicked = [];
  				$scope.commitMessage();
  				$scope.historyResource();
  			},$scope.time*1000);
  		}
  	};
  	
  	$scope.autoreload();
  	
  	$scope.getRefreshColor = function() {
  		return $scope.color;
  	};
  	
  	$scope.blurAfterEnter = function($event) {
  		if ( $event.keyCode == 13 )
			return $event.target.blur();
  	};
	
	$scope.refresh = function() {
		$http({
			url : "rest/commit-items",
			method : "GET",
			dataType : "json",
			headers : {
				"Content-Type" : "application/json; charset=utf-8"
			}
		}).success(function(data) {
			$scope.items = data;
			angular.forEach($scope.items, function(item) {
				createDateStringAttributes(item);
			});
			$scope.search();
		}).error(function(data) {
			alert('err');
		});
	};
	
	$scope.context = function(status,committer,protectedBranch,branchId,commitId) {
		if(status == 'FAILED') {
			$scope.branchDescriptor = sharedProperty.setbranchDescriptor(committer,protectedBranch,branchId,commitId);
			$scope.menuOptions = [
			                       ['Force push', function () {
			                    	   showModalWindow();
			                       }]
			                   ];
		}
		else {
			$scope.menuOptions = null;
		}
	};

	var searchMatch = function(haystack, needle) {

		if (!needle) {
			return true;
		}
		if (!haystack) {
			return false;
		}

		return haystack.toString().toLowerCase().indexOf(needle.toLowerCase()) !== -1;
	};

	// init the filtered items
	$scope.search = function() {

		$scope.filteredItems = $filter('filter')($scope.items, function(item) {
			for (var attr in item) {
				if (attr === 'branchDescriptor') {
					for (var attr2 in item[attr]) {
						if (searchMatch(item[attr][attr2], $scope.query)) {
							return true;
						}
					}
				} else {
					if (attr !== 'creationTime' && attr !== 'runTime' && attr !== 'endTime') {
						if (searchMatch(item[attr], $scope.query)) {
							return true;
						}
					}
				}
			}
			return false;
		});
		// take care of the sorting order
		if ($scope.sortingOrder !== '') {
			$scope.filteredItems = $filter('orderBy')($scope.filteredItems, $scope.sortingOrder, $scope.reverse);
		}
		$scope.currentPage = 0;
		// now group by pages
		$scope.groupToPages();
	};
	
	function createDateStringAttributes(commitItem) {
		commitItem.creationDateTimeString = formatDate(commitItem.creationTime);
		commitItem.runDateTimeString = formatDate(commitItem.runTime);
		commitItem.endDateTimeString = formatDate(commitItem.endTime);
	};
	
	function formatDate(date) {
		date = new Date(date).toString("dd-MM-yyyy HH:mm:ss");
		if(date == "01-01-1970 02:00:00") {
			return "N/A";
		}
		else {
			return date;
		}
	};

	// calculate page in place
	$scope.groupToPages = function() {
		$scope.pagedItems = [];

		for (var i = 0; i < $scope.filteredItems.length; i++) {
			if (i % $scope.itemsPerPage === 0) {
				$scope.pagedItems[Math.floor(i / $scope.itemsPerPage)] = [$scope.filteredItems[i]];
			} else {
				$scope.pagedItems[Math.floor(i / $scope.itemsPerPage)].push($scope.filteredItems[i]);
			}
		}
	};
	
	$scope.isParentBranchValid = function (parentBarnchName){
		if (parentBarnchName.match(/^(refs\/heads\/)/g) ){
			return parentBarnchName.substring(11);
		}else {
			return parentBarnchName;
		};
		
	};

	$scope.range = function(start, end) {
		var ret = [];
		if (!end) {
			end = start;
			start = 0;
		}
		for (var i = start; i < end; i++) {
			ret.push(i);
		}
		return ret;
	};

	$scope.prevPage = function() {
		if ($scope.currentPage > 0) {
			$scope.currentPage--;
		}
	};

	$scope.nextPage = function() {
		if ($scope.currentPage < $scope.pagedItems.length - 1) {
			$scope.currentPage++;
		}
	};

	$scope.setPage = function() {
		$scope.currentPage = this.n;
	};

	// functions have been describe process the data for display
	$scope.refresh();

	// change sorting order
	$scope.sort_by = function(newSortingOrder) {
		if ($scope.sortingOrder == newSortingOrder) {
			$scope.reverse = !$scope.reverse;
		} else {
			$scope.sortingOrder = newSortingOrder;
			$scope.reverse = false;
		}
	};

	$scope.getColor = function(status) {

		var ret = 'black';
		if (status == 'PASSED_BY_CHILD' || status == 'PASSED_AND_PUSHED') {
			ret = 'green';
		} else if (status == 'FAILED_AND_PUSHED' || status == 'FORCING_PUSH') {
			ret = 'FF6600';
		} else if (status != 'RUNNING' && status != 'NOT_STARTED' && status != 'PASSED') {
			ret = 'red';
		}
		return ret;
	};

	$scope.button  = function(status,commitId,mode,runTime,endTime) {
		if($scope.clicked.length != 0){
		for (var d = 0, len = $scope.clicked.length; d < len; d++) {
			if($scope.clicked[d] == commitId || mode=='enabled' &&(status !='FAILED' && status !='TRIGGER_FAILED' && status !='TIMEOUT' && status !='GIT_FAILURE'))
			{	
				return 	true;
			}
			else if($scope.clicked[d] == commitId || mode=='disabled' &&(status !='TRIGGER_FAILED' && status !='TIMEOUT' && status !='GIT_FAILURE'))
			{	
				return 	true;
			}
		}
		}
		else {
			if(mode=='enabled' &&(status !='FAILED' && status !='TRIGGER_FAILED' && status !='TIMEOUT' && status !='GIT_FAILURE'))
			{	
				return 	true;
			}
			else if(mode=='disabled' &&(status !='TRIGGER_FAILED' && status !='TIMEOUT' && status !='GIT_FAILURE'))
			{	
				return 	true;
			}
			if(runTime>endTime) {
				return true;
			}
		}
	};
 
	 $scope.retry = function(committer,protectedBranch,branchId,commitId) {
	        var branchDescriptor = {   
	          committer: committer,
	          protectedBranch: protectedBranch,
	          newBranch: branchId,
	          commitId: commitId
	        };
	        $scope.clicked.push(commitId);
	        $http({
	               url : "rest/branches",
	               method : "POST",
	               dataType : "json",
	               data : branchDescriptor,
	               headers : {
	                     "Content-Type" : "application/json; charset=utf-8",
	                     "Accept" : "application/json"
	               }
	        }).success(function(data) {
	               $scope.items = data;
	        }).error(function(data) {
	               alert('err');
	        });
	 };
	 
	 function showModalWindow() {
		    ModalService.showModal({
		      templateUrl: "modal.html",
		      controller: "ModalController",
		    }).then(function(modal) {
		      modal.element.modal();
		      modal.close();
		    });
	  };
	  
	  $scope.historyResource = function() {
		  $http({
              url : "rest/historyData",
              method : "GET",
              dataType : "json",
              headers : {
                          "Content-Type" : "application/json; charset=utf-8"
                        }
              }).success(function(data) {
                        $scope.historyData = data;
                    	$scope.allHistory = [];
                        angular.forEach($scope.historyData, function(hist) {
                        	$scope.historyValues = hist;
                        	angular.forEach($scope.historyValues, function(value) {
                        		historyDate(value);
                        		$scope.allHistory.push(value);
                        	});
                        });
              }).error(function(data) {
                        alert('err');
              });
	  };
	  
	  $scope.historyResource();
	  
	  historyDate = function(hitem){
	      hitem.endDateTimeString = formatDate(hitem.endTime);
      };
	  
	  $scope.historyExist = function(commitId) {
		  $scope.histExist = true;
		  angular.forEach($scope.allHistory, function(history) {
			  if(history.commitId === commitId) {
				  $scope.histExist = false;
			  }
		  });
		  return $scope.histExist;
	  };
	  
	  $scope.getHistory = function(commitId) {
		  $scope.histNumber = 0;
		  angular.forEach($scope.allHistory, function(history) {
			  if(history.commitId === commitId) {
				  $scope.histNumber++;
			  }
		  });
		  return $scope.histNumber;
	  };
	 
	  $scope.saveHistory = function(id,show) {
		  $cookieStore.put(id.toString(),show);  
	  };
	  
	  $scope.getValue = function(commitId) {
		  if($cookieStore.get(commitId.toString()) == false) {
			  return false;
		  }
		  else {
			  return true;
		  }
	  };
	  
	  $scope.expandAll = function(value) {
		  angular.forEach($scope.allHistory, function(history) {
			  $cookieStore.put(history.commitId.toString(),value);
		  });
		  $cookieStore.put("expandAll",value);
	  };
	  
	  $scope.getAll = function() {
		  if($cookieStore.get("expandAll") == false) {
			  $scope.expandCollapse = "Collapse All";
			  return false;
		  }
		  else {
			  $scope.expandCollapse = "Expand All";
			  return true;
		  }
	  };

	  $scope.displayMode = function() {
	  	  $http({
	              url : "rest/uidisplaymode",
	              method : "GET",
	              dataType : "json",
	              headers : {
	                          "Content-Type" : "application/json; charset=utf-8"
	                        }
	              }).success(function(data) {
	            	  $scope.displayMode = data;
	              }).error(function(data) {
	            	  alert('err');
	              });
	       }; 
	                                                                  	
	$scope.displayMode();

	$scope.commitMessage = function() {
		$http({
			url : "rest/commit-message",
			method : "GET",
			dataType : "json",
			headers : {
				"Content-Type" : "application/json; charset=utf-8"
			}
		}).success(function(data) {
			$scope.messages = data;
		}).error(function(data) {
			alert('err');
		});
	};
	
	$scope.commitMessage();
	
	$scope.getMessage = function(branchId) {
		for(var attr in $scope.messages) {
			if(attr.substr(0,7) == branchId) {
				$scope.dynamicTooltip = $scope.messages[attr];
			}
		}
	};

}
]);

app.controller('ModalController', [ '$scope', '$http', 'close',
		'sharedProperty', function($scope, $http, close, sharedProperty) {
			$scope.open = true;
			$scope.close = function() {
				close(500); // close, but give 500ms for bootstrap to animate
				$scope.open = false;
			};

			$scope.checkPassword = function(password) {
				$scope.branchDescriptor = sharedProperty.getbranchDescriptor();
				$http({
					url : "rest/branches",
					method : "POST",
					dataType : "json",
					data : $scope.branchDescriptor,
					params : password,
					headers : {
						"Content-Type" : "application/json; charset=utf-8",
						"Accept" : "application/json"
					}
				}).success(function(data) {
					$scope.items = data;
					$scope.close();
				}).error(function(data) {
					alert('The password is incorrect!');
				});
			};

		} ]);