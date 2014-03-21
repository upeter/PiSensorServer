angular.module('stockMarketApp')
    .factory('AlertService',function () {
        var message;
        return {
            set: function (msg) {
                message = msg;
            },
            clear: function () {
                message = null;
            },
            get: function () {
                return message;
            }
        };
    }).factory('SensorService2',function () {
    	 var value;
         return {
             get: function (sensor) {
             	var min = 0;
             	var max = 950;
             	var random = Math.floor(Math.random() * (max - min + 1)) + min;
             	value = random;
             	//{value:100,indicator:CRITICAL,LOW, MEDIUM, HIGH,TOO_HIGH}
             	var status = 'Low';
             	if(value > 400) {
             		status = 'High';
             	}
             	var res = {};
             	res.value = value;
             	res.status = status
             	return res;
             }
         };
    })
    .factory('SensorService', ['$http', function ($http) {
    	 var value;
        return {
            get: function (sensorName) {
            	return $http.get('/sensor/' + sensorName);
//            	var min = 0;
//            	var max = 950;
//            	var random = Math.floor(Math.random() * (max - min + 1)) + min;
//            	value = random;
//            	return value;
            }
        };
    }]).factory('StockService', ['$http', function ($http) {

        return {
            query: function () {
                return $http.get('/api/stocks');
            },
            dashboard: function () {
                return $http.get('/api/dashboard');
            },
            get: function (code) {
                return $http.get('/api/stocks/' + code);
            },
            toggleFavorite: function (stockCode) {
                return $http.post('/api/favorite', {stockCode: stockCode});
            }
        };
    }]).factory('UserService', ['$http', '$q', function ($http, $q) {
        var user = {};
        var loggedIn = false;
        var loginSuccess = function (resp) {
            user = resp.data.user;
            loggedIn = true;
            return user;
        };
        var loginFailure = function (err) {
            loggedIn = false;
            console.log('Rejecting');
            return $q.reject(err.data);
        };

        return  {
            isLoggedIn: function () {
                return loggedIn;
            },
            login: function (username, pwd) {
                return $http.post('/api/login', {username: username, password: pwd}).then(loginSuccess, loginFailure);
            },
            logout: function () {
                return $http.post('/api/logout', {}).then(function () {
                    loggedIn = false;
                }, function () {
                    loggedIn = false;
                });
            },
            register: function (username, pwd) {
                return $http.post('/api/register', {username: username, password: pwd}).then(loginSuccess, loginFailure);
            },
            tokens: function () {
                if (loggedIn) {
                    return $q.when(user);
                } else {
                    return $http.post('/api/token', {}).then(loginSuccess, loginFailure);
                }
            }
        };
    }]).factory('EventService', ['$http', function ($http) {
        var date = new Date();
        var d = date.getDate();
        var m = date.getMonth();
        var y = date.getFullYear();

        var events = [
            {
                title: 'All Day Event',
                start: new Date(y, m, 1)
            },
            {
                title: 'Long Event',
                start: new Date(y, m, d - 5),
                end: new Date(y, m, d - 2)
            },
            {
                title: 'Repeating Event',
                start: new Date(y, m, d - 3, 16, 0),
                allDay: false
            },
            {
                title: 'Repeating Event',
                start: new Date(y, m, d + 4, 16, 0),
                allDay: false
            },
            {
                title: 'Meeting',
                start: new Date(y, m, d, 10, 30),
                allDay: false
            },
            {
                title: 'Lunch',
                start: new Date(y, m, d, 12, 0),
                end: new Date(y, m, d, 14, 0),
                allDay: false
            },
            {
                title: 'Birthday Party',
                start: new Date(y, m, d + 1, 19, 0),
                end: new Date(y, m, d + 1, 22, 30),
                allDay: false
            },
            {
                title: 'Click for Google',
                start: new Date(y, m, 28),
                end: new Date(y, m, 29),
                url: 'http://google.com/'
            }
        ];
        return {
            get: function () {
                return events;
            },
            add: function (event) {
                events.push(event);
            },
            update: function (event) {
                for (var i = 0; i < events.length; i++) {
                    if (events[i].title == event.title) {
                        events[i] = event;
                        break;
                    }
                }
            }
        }
    }])

