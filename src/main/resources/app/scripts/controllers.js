angular.module('stockMarketApp')
  .controller('AuthCtrl', ['$location', 'AlertService', 'UserService', function ($location, AlertService, UserService) {

    var self = this;
    self.login = function() {
      UserService.login(self.username, self.password).then(function(user) {
        $location.path('/mine');
      }, function(err) {
        AlertService.set(err.msg);
      });
    };
    self.register = function() {
      UserService.register(self.username, self.password).then(function(user) {
        $location.path('/mine');
      }, function(err) {
        AlertService.set(err.data.msg);
      });
    };
  }])
  .controller('AppCtrl', ['AlertService', 'UserService', function(AlertService, UserService) {
    this.alertService = AlertService;
    this.userService = UserService;
    this.dateSelected = function(dateVal) {
      console.log("Date ", dateVal, " has just been selected");
    };

  }])
  .controller('LandingCtrl', ['StockService', 'SensorService', '$interval', function(StockService, SensorService, $interval) {
    var alertData = {};
    alertData['Low'] = {title:'Your plant is drying out', message:'Water her immediately!', style:'alert alert-danger'};
    alertData['Medium'] = {title:'Your plant is thirsty', message:'Grant her some water', style:'alert alert-warning'};
    alertData['High'] = {title:'Your plant is happy', message:'Go do something else', style:'alert alert-success'};
    alertData['TooHigh'] = {title:'Your plant is drowning', message:"Don\'t water her anymore!", style:'alert alert-info'};
    alertData['Unkown'] = {title:'Unkown plant state', message:'Wait or check the sensor connection', style:'alert alert-danger'};
    
	var self = this;
    self.stocks = [];
    self.humidity = {status:'Unkown', value:0};
    self.alertTitle = alertData['Unkown'].title;
    self.alertMessage = alertData['Unkown'].message;
    	
    var fetchGaugeData = function () {
    	SensorService.get('humidity').success(function(result) {
    		 self.humidity = result
    		 self.alertTitle = alertData[result.status].title;
    		 self.alertMessage = alertData[result.status].message;
    	 });
   };
   self.getAlertClass = function() {
	   return alertData[self.humidity.status].style;
   }
   
   fetchGaugeData();
   $interval(fetchGaugeData, 5000);

  }])
  .controller('LogoutCtrl', ['UserService', '$location', function(UserService, $location) {
    UserService.logout().then(function() {
      $location.path('/all');
    });
  }])
  .controller('MyStocksCtrl', ['StockService', function(StockService) {
    var self = this;

    self.stocks = [];
    self.fetchStocks = function() {
      StockService.query().success(function(stocks) {
        self.stocks = stocks;
      });
    };
    self.fetchStocks();

    self.filters = {
      favorite: true
    };

    self.toggleFilter = function() {
      if (self.filters.favorite) {
        delete self.filters.favorite;
      } else {
        self.filters.favorite = true;
      }
    };

  }])
    .controller('CalendarCtrl', ['EventService', function(EventService) {
       var self = this;
        self.onCreate= function(event) {
            console.log('onCreate ', self.events.length);
            EventService.add(event);
            console.log('AFTER Create ', self.events.length);
        }
        self.onUpdate = function(event) {
            console.log('onUpdate ', self.events.length);
            EventService.update(event);
            console.log('AFTER Update ', self.events.length);
        }
        self.events = EventService.get();
    }])
    .controller('EventCtrl', ['$scope','$modalInstance', 'UserService', 'AlertService', 'EventService', 'event', function($scope, $modalInstance, UserService, AlertService, EventService, event) {
        var self = this;
        self.save = function () {
            console.log('save')
                $modalInstance.close(event);
        };
        self.delete = function () {
            console.log('delete')
            $modalInstance.close(event);
        };
        $scope.cancel = function () {
            AlertService.clear();
            console.log('cancel')
            $modalInstance.dismiss('cancel');
        };
    }])
    .controller('ModalLoginCtrl', ['$scope','$modal', '$log', function ModalLoginCtrl($scope, $modal, $log) {
        $scope.items = ['item1', 'item2', 'item3'];

        $scope.open = function () {
            var modalInstance = $modal.open({
                templateUrl: 'views/login_modal.html',
                controller: 'ModalLoginInstanceCtrl',
                resolve: {
                    items: function () {
                        return $scope.items;
                    }
                }
            });

            modalInstance.result.then(function (user) {
                $location.path('/mine');
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };
    }])
    //items are passed in from 'resolve' in ModalDemoCtrl
    .controller('ModalLoginInstanceCtrl', ['$scope','$modalInstance', 'UserService', 'AlertService','items', function ModalLoginInstanceCtrl($scope, $modalInstance, UserService, AlertService, items) {
        var self = this;
        $scope.loginCtrl = self;
        self.alertService = AlertService;
        self.login = function () {
            UserService.login(self.username, self.password).then(function (user) {
                AlertService.clear();
                $modalInstance.close(user);
            }, function (err) {
                AlertService.set(err.data.msg);
            });
        };
        self.cancel = function () {
            AlertService.clear();
            console.log('cancel')
            $modalInstance.dismiss('cancel');
        };
    }]);

