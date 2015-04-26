'use strict'

requirejs.config(
  paths:
    'angular': ['../lib/angularjs/angular'],
    'angular-resource': ['../lib/angularjs/angular-resource'],
    'angular-ui-bootstrap': ['../lib/angular-ui-bootstrap/ui-bootstrap-tpls']
    'angular-ui-router': ['../lib/angular-ui-router/angular-ui-router']
    'web': ['../javascripts/web']
  shim:
    'angular':
      exports: 'angular'
    'angular-resource': ['angular']
    'angular-ui-bootstrap': ['angular']
    'angular-ui-router': ['angular']
)

require([
    'angular',
    'angular-resource',
    'angular-ui-bootstrap',
    'angular-ui-router',
    'web'
  ], (angular) ->

  ad2460 = angular.module('ad2460', ['ngResource', 'ui.bootstrap', 'ui.router', 'web'])

  ad2460.factory 'ShipsDamageResource',
    ($resource) ->
      $resource("/api/data/shipsDamage")

  ad2460.factory 'ShipsResource',
    ($resource) ->
      $resource("/api/data/ships")

  ad2460.factory 'TacticalResource',
    ($resource) ->
      $resource("api/battles/tacticalSettings")

  ad2460.factory 'BattleReportsResource',
    ($resource) ->
      $resource("api/battles/report/:id", {id:'@id'})

  ad2460.config ($stateProvider, $urlRouterProvider, $locationProvider) ->
    $urlRouterProvider
      .otherwise('/home')

    $stateProvider
      .state('home',
        url: '/home'
        templateUrl: '/views/home'
        resolve:
          battleReportsResource: 'BattleReportsResource'
          battleReports: (battleReportsResource) ->
            battleReportsResource.query().$promise
        controller: 'HomeCtrl'
      ).state('ships',
        url: '/ships'
        templateUrl: '/views/ships'
        resolve:
          shipsResource: 'ShipsResource'
          ships: (ShipsResource) ->
            ShipsResource.query().$promise
        controller: 'ShipsCtrl'
      ).state('shipsDamage',
        url: '/shipsDamage'
        templateUrl: '/views/shipsDamage'
        resolve:
          shipsDamageResource: 'ShipsDamageResource'
          ships: (ShipsDamageResource) ->
            ShipsDamageResource.query().$promise
        controller: 'ShipsDamageCtrl'
      ).state('battleReport',
        url: '/battles/report/:reportId'
        templateUrl: '/views/report'
        resolve:
          battleReportsResource: 'BattleReportsResource'
          battleReport: (battleReportsResource, $stateParams) ->
            battleReportsResource.get({id: $stateParams.reportId}).$promise
        controller: 'BattleReportCtrl'
      ).state('newReport',
        url: '/newReport'
        templateUrl: '/views/newReport'
        resolve:
          tacticsResource: 'TacticalResource'
          tactics:  (tacticsResource) ->
            tacticsResource.query().$promise
        controller: 'NewReportCtrl'
      )

    $locationProvider.html5Mode(true).hashPrefix('!')

  angular.bootstrap(document, ['ad2460'])
)