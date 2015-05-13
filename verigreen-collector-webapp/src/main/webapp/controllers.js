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
app.controller('ctrlRead', ['$scope','$filter','$http', 'ModalService', 'sharedProperty', '$cookies', '$interval',
   function ($scope, $filter, $http, ModalService, sharedProperty, $cookies,$interval) {

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
	$scope.check = false;
	$scope.number = /^\d+$/;
	$scope.time = "30";
	$scope.checklist = true;
	$scope.showHist = true;
	$scope.color = "gray";
	
    $scope.reloadValue = function() {
    	if($cookies.History == "false") {
    		$scope.showHistory = false;
    	} else if($cookies.History == "true") {
    		$scope.showHistory = true;
    	}
    	if($cookies.Id == "false") {
    		$scope.showId = false;
    	} else if($cookies.Id == "true") {
    		$scope.showId = true;
    	}
    	if($cookies.ProtectedBranch == "false") {
    		$scope.showProtectedBranch = false;
    	} else if($cookies.ProtectedBranch == "true") {
    		$scope.showProtectedBranch = true;
    	}
    	if($cookies.ParentBranch == "false") {
    		$scope.showParentBranch = false;
    	} else if($cookies.ParentBranch == "true") {
    		$scope.showParentBranch = true;
    	}
    	if($cookies.Committer == "false") {
    		$scope.showCommitter = false;
    	} else if($cookies.Committer == "true") {
    		$scope.showCommitter = true;
    	}
    	if($cookies.Status == "false") {
    		$scope.showStatus = false;
    	} else if($cookies.Status == "true") {
    		$scope.showStatus = true;
    	}
    	if($cookies.Retry == "false") {
    		$scope.showRetry = false;
    	} else if($cookies.Retry == "true") {
    		$scope.showRetry = true;
    	}
    	if($cookies.CreationTime == "false") {
    		$scope.showCreationTime = false;
    	} else if($cookies.CreationTime == "true") {
    		$scope.showCreationTime = true;
    	}
    	if($cookies.RunTime == "false") {
    		$scope.showRunTime = false;
    	} else if($cookies.RunTime == "true") {
    		$scope.showRunTime = true;
    	}
    	if($cookies.EndTime == "false") {
    		$scope.showEndTime = false;
    	} else if($cookies.EndTime == "true") {
    		$scope.showEndTime = true;
    	}
    	if($cookies.BuildUrl == "false") {
    		$scope.showBuildUrl = false;
    	} else if($cookies.BuildUrl == "true") {
    		$scope.showBuildUrl = true;
    	}
    };
    
    $scope.reloadValue();
    
    $scope.toogleHistory = function(){
    	$scope.showHistory=!$scope.showHistory;
    	if($scope.showHistory == false) {
    		$cookies.History = "false";
    	}
    	else {
    		$cookies.History = "true";
    	}
    };
	
    $scope.toogleId = function(){
    	$scope.showId=!$scope.showId;
    	if($scope.showId == false) {
    		$cookies.Id = "false";
    	}
    	else {
    		$cookies.Id = "true";
    	}
    };
    
    $scope.toogleProtectedBranch = function(){
    	$scope.showProtectedBranch=!$scope.showProtectedBranch;
    	if($scope.showProtectedBranch == false) {
    		$cookies.ProtectedBranch = "false";
    	}
    	else {
    		$cookies.ProtectedBranch = "true";
    	}
    };
    
    $scope.toogleParentBranch = function(){
    	$scope.showParentBranch=!$scope.showParentBranch;
    	if($scope.showParentBranch == false) {
    		$cookies.ParentBranch = "false";
    	}
    	else {
    		$cookies.ParentBranch = "true";
    	}
    };
    
    $scope.toogleCommitter = function(){
    	$scope.showCommitter=!$scope.showCommitter;
    	if($scope.showCommitter == false) {
    		$cookies.Committer = "false";
    	}
    	else {
    		$cookies.Committer = "true";
    	}
    };
    
    $scope.toogleStatus = function(){
    	$scope.showStatus=!$scope.showStatus;
    	if($scope.showStatus == false) {
    		$cookies.Status = "false";
    	}
    	else {
    		$cookies.Status = "true";
    	}
    };
    
    $scope.toogleRetry = function(){
    	$scope.showRetry=!$scope.showRetry;
    	if($scope.showRetry == false) {
    		$cookies.Retry = "false";
    	}
    	else {
    		$cookies.Retry = "true";
    	}
    };
    
    $scope.toogleCreationTime = function(){
    	$scope.showCreationTime=!$scope.showCreationTime;
    	if($scope.showCreationTime == false) {
    		$cookies.CreationTime = "false";
    	}
    	else {
    		$cookies.CreationTime = "true";
    	}
    };
    
    $scope.toogleRunTime = function(){
    	$scope.showRunTime=!$scope.showRunTime;
    	if($scope.showRunTime == false) {
    		$cookies.RunTime = "false";
    	}
    	else {
    		$cookies.RunTime = "true";
    	}
    };
    
    $scope.toogleEndTime = function(){
    	$scope.showEndTime=!$scope.showEndTime;
    	if($scope.showEndTime == false) {
    		$cookies.EndTime = "false";
    	}
    	else {
    		$cookies.EndTime = "true";
    	}
    };
    
    $scope.toogleBuildUrl = function(){
    	$scope.showBuildUrl=!$scope.showBuildUrl;
    	if($scope.showBuildUrl == false) {
    		$cookies.BuildUrl = "false";
    	}
    	else {
    		$cookies.BuildUrl = "true";
    	}
    };
    
    $scope.toogle = function() {
  		$scope.check =!$scope.check;
  		if($scope.check == true){
  			$scope.color = "green";
  			$cookies.time = $scope.time;
  			timer = $interval(function(){
  				$scope.refresh();
  				$scope.clicked = [];
  				$scope.commitMessage();
  				$scope.historyResource();
  			},$scope.time*1000);
  		}
  		else if($scope.check == false){
  			$scope.color = "gray";
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
  			$scope.color = "green";
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
				createDateStringAttributes(item);
				if (attr === 'branchDescriptor') {
					for (var attr2 in item[attr]) {
						if (searchMatch(item[attr][attr2], $scope.query)) {
							return true;
						}
					}
				} else if (searchMatch(item[attr], $scope.query)) {
					return true;
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
		
		commitItem.creationTimeStr = toDateString(commitItem.creationTime);
		commitItem.runTimeStr = toDateString(commitItem.runTime);
		commitItem.endTimeStr = toDateString(commitItem.endTime);
	}

	function toDateString(timestamp) {

		return new Date(timestamp).toString("dd-MM-yyyy HH:mm:ss");
	}

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
		for (var d = 0, len = $scope.clicked.length; d < len; d += 1) {
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
              }).error(function(data) {
                        alert('err');
              });
	  };
	  
	  $scope.historyResource();
	  
	  $scope.formatDate = function(date){
          return new Date(date);
    };
	  
	  $scope.historyExist = function(commitId) {
		  $scope.histExist = true;
		  for (var d = 0, len = $scope.historyData.length; d < len; d += 1) {
              if ($scope.historyData[d].commitId === commitId) {
                  $scope.histExist = false; 
              }
          }
		  return $scope.histExist;
	  };
	  
	  $scope.getHistory = function(commitId) {
		  $scope.histNumber = 0;
		  for (var d = 0, len = $scope.historyData.length; d < len; d += 1) {
              if ($scope.historyData[d].commitId === commitId) {
                  $scope.histNumber++;
              }
          }
		  return $scope.histNumber;
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
			if(attr.substr(0,7)==branchId) {
				$scope.dynamicTooltip =  $scope.messages[attr];
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