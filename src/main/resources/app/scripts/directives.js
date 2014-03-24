angular.module('stockMarketApp').directive('stockDash', ['UserService', 'StockService', '$interval', function (UserService, StockService, $interval) {
        return {
            restrict: 'A',
            templateUrl: 'views/stock-dash.html',
            scope: {
                stockData: '=',
                whenToggle: '&'
            },
            link: function ($scope, $element, $attrs) {

                $scope.getChange = function () {
                    return Math.ceil((($scope.stockData.price - $scope.stockData.previous) / $scope.stockData.previous) * 100);
                };
                $scope.getChangeClass = function () {
                    return $scope.getChange() >= 0 ? 'positive' : 'negative';
                };
                $scope.toggleFavorite = function () {
                    StockService.toggleFavorite($scope.stockData.ticker).then(function (stocks) {
                        if ($scope.whenToggle) {
                            $scope.whenToggle();
                        }
                    }, function (err) {
                    });
                };
                $scope.shouldShowButtons = function () {
                    return UserService.isLoggedIn();
                };

                var fetchStockDetail = function () {
                    StockService.get($scope.stockData.ticker).success(function (stockData) {
                        $scope.stockData.history = stockData.history;
                        $scope.stockData.price = stockData.price;
                        $scope.stockData.previous = stockData.previous;

                    });
                };
                $interval(fetchStockDetail, 5000);
            }
        };
    }]).directive('lineChart', ['$timeout', '$window', function ($window) {
        return {
            restrict: 'A',
            scope: {
                graphData: '='
            },
            link: function ($scope, $element, $attrs) {


                var dataToArray = function (priceHistory) {
                    var arr = [
                        ['Index', 'Price']
                    ];
                    for (var i = 0; i < priceHistory.length; i++) {
                        arr.push([i, priceHistory[i]]);
                    }
                    return arr;
                };

                var drawChart = function () {
                    var chart = new google.visualization.LineChart($element[0]);
                    $scope.$watch('graphData', function (newVal) {
                        if (newVal) {
                            chart.draw(google.visualization.arrayToDataTable(dataToArray(newVal)), {
                                height: 70,
                                legend: {position: 'none'}
                            });
                        }

                    }, true);
                };
                drawChart();

            }
        };
    }]).directive('gaugeMeter', ['SensorService', '$timeout', '$window', function (SensorService, $window) {
        return {
            restrict: 'A',
            scope: {
                gaugeData: '='
            },
            link: function ($scope, $element, $attrs) {
            	  var dataToArray = function (humidity) {
                      var arr = [
                         ['Label', 'Value'],
                         ['Humidity', humidity]
                      ];
                     // for (var i = 0; i < priceHistory.length; i++) {
                       //   arr.push([i, priceHistory[i]]);
                      //}
                      return arr;
                  };
            	
            	var drawChart = function() {
            	        //# 0  ~300     dry soil
            	        //# 300~700     humid soil
            	        //# 700~950     in water

            	        var options = {
            	          width: 400, height: 200,
            	          min:0, max:950,
            	          redFrom: 0, redTo: 250,
            	          yellowFrom:250, yellowTo: 450,
            	          greenFrom:450, greenTo:750,
            	          minorTicks: 5
            	        };

            	        var chart = new google.visualization.Gauge($element[0]);
            	        //chart.draw(data, options);
            	        $scope.$watch('gaugeData', function (newVal) {
                            if (newVal) {
                            	chart.draw(google.visualization.arrayToDataTable(dataToArray(newVal.value)), options);
                            }
                        }, true);
            	      };
            	drawChart();
            	
            	//=======================
            }
        };
    }]).directive('datepicker', [function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                whenSelect: '&'
            },
            link: function ($scope, $element, $attrs, ngModelCtrl) {

                var configObj = {
                    onSelect: function (dateTxt) {
                        ngModelCtrl.$setViewValue(dateTxt);
                        if ($scope.whenSelect) {
                            $scope.whenSelect({date: dateTxt});
                        }

                        $scope.$apply();
                    }
                };

                ngModelCtrl.$render = function () {
                    $element.datepicker('setDate', ngModelCtrl.$viewValue);
                };

                $element.datepicker(configObj);
            }
        };
    }])
    .directive('jquiCalendar', ['$modal', function ($modal) {
        return {
            restrict: 'A',
            scope: {
                events: '=',
                whenCreated: '&',
                whenUpdated: '&'
            },
            link: function ($scope, $element, $attrs) {
                var date = new Date();
                var d = date.getDate();
                var m = date.getMonth();
                var y = date.getFullYear();
                var configObj = {
                    header: {
                        left: 'prev,next today',
                        center: 'title',
                        right: 'month,agendaWeek,agendaDay'
                    },
                    defaultView: 'agendaDay',
                    selectable: true,
                    selectHelper: true,
                    eventResize: function (event, dayDelta, minuteDelta, revertFunc) {
                        $scope.whenUpdated({event: event, undoFunc:revertFunc});
                    },
                    eventDrop: function (event, dayDelta, minuteDelta, allDay, revertFunc) {
                        $scope.whenUpdated({event: event, undoFunc:revertFunc});
                    },
                    eventClick: function(calEvent, jsEvent, view) {
                            var modalInstance = $modal.open({
                                templateUrl: 'views/event.html',
                                controller: 'EventCtrl',
                                resolve: {
                                    event: function () {
                                        console.log(calEvent);
                                        return calEvent;
                                    }
                                }
                            });

                            //alert('you clicked', calEvent)
                    },
                        select: function (start, end, allDay) {
                        var title = prompt('Event Title:');
                        if (title) {
                            var event = {
                                title: title,
                                start: start,
                                end: end,
                                allDay: allDay
                            };

                            $scope.whenCreated({event: event});
                            $scope.$apply();
                        }
                        $element.fullCalendar('unselect');

                    },
                    editable: true//,
                    //  events: $scope.events

                };
                $element.fullCalendar(configObj);

                $scope.$watchCollection('events', function (newVal) {
                    if (newVal) {
                        $element.fullCalendar('removeEvents');
                        $element.fullCalendar('addEventSource', newVal);
                        console.log('here', newVal);
                    }

                });
            }
        };
    }]);
