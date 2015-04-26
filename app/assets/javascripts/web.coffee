'use strict'

define(['angular'], (angular) ->

  web = angular.module('web', ['ngResource'])

  web.controller 'HomeCtrl',
    class HomeCtrl
      constructor: ($scope, $http, $state, battleReports) ->

        $scope.entry = ''
        $scope.title = ''

        $scope.reports = battleReports

        $scope.battleReport = (r) ->
          window.location.assign('/api/battles/report/' + r)

        $scope.deleteReport = (r) ->
          console.log(r)
          $http.post 'api/battles/report/delete', { reportId: r
          }
          .success () ->
            $state.go($state.$current, null, {reload: true})
#          window.location.assign('/api/battles/report/' + r)

  web.controller 'ShipsDamageCtrl',
    class ShipsDamageCtrl
      constructor: ($scope, ships) ->
        $scope.ships = ships

  web.controller 'ShipsCtrl',
    class ShipsCtrl
      constructor: ($scope, ships) ->
        $scope.ships = ships
        $scope.att = {}
        $scope.att.range = 'Ran'
        $scope.att.targeting = 'Tar'
        $scope.att.guns = 'Gun'
        $scope.att.rateOfFire = 'RoF'
        $scope.att.critChance = 'Cri'
        $scope.att.damage = 'Dmg'
        $scope.att.averageDamage = 'AvDmg'
        $scope.def = {}
        $scope.def.hull = 'Hul'
        $scope.def.shield = 'Shl'
        $scope.def.absorbs = 'Abs'
        $scope.def.shieldRecharge = 'Reg'
        $scope.def.maneuverability = 'Man'

  web.controller 'NewReportCtrl',
    class NewReportCtrl
      constructor: ($scope, $http, $state, tactics) ->

        $scope.tacticalOptions = {}
        $scope.tacticalOptions.targetPriority = tactics[0][0].targetPriority
        $scope.tacticalOptions.targetReEvaluate = tactics[0][1].targetReEvaluate
        $scope.tacticalOptions.settingsFighter = tactics[0][2].settingsFighter
        $scope.tacticalOptions.settingsCorvette = tactics[0][3].settingsCorvette
        $scope.tacticalOptions.retreat1 = tactics[0][4].retreat1
        $scope.tacticalOptions.retreat2 = tactics[0][5].retreat2
        $scope.atactics = [
          $scope.tacticalOptions.targetPriority[0]
          $scope.tacticalOptions.targetReEvaluate[0]
          $scope.tacticalOptions.settingsFighter[0]
          $scope.tacticalOptions.settingsCorvette[0]
          $scope.tacticalOptions.retreat1[0]
          $scope.tacticalOptions.retreat2[0]
        ]
        $scope.dtactics = [
          $scope.tacticalOptions.targetPriority[0]
          $scope.tacticalOptions.targetReEvaluate[0]
          $scope.tacticalOptions.settingsFighter[0]
          $scope.tacticalOptions.settingsCorvette[0]
          $scope.tacticalOptions.retreat1[0]
          $scope.tacticalOptions.retreat2[0]
        ]

        $scope.title = ''
        $scope.battleId = ''

        $scope.attackerDetails = ''
        $scope.attackerName = ''
        $scope.defenderDetails = ''
        $scope.defenderName = ''
        $scope.combatDetails = ''

        r1 = ['<div style="width: 650px; position: relative;">',
              '<span class="combat_defender_text">',
              '<span class="combat_attacker_text">',
              '</div>', '</span>']
        r2 = ['<s>', '<d>', '<a>', '<e>', '']

        $scope.newReport = () ->
          if ($scope.reportForm.$valid)
            atactics = $scope.atactics.map (e , i) ->
              $scope.atactics[i].key
            dtactics = $scope.dtactics.map (e , i) ->
              $scope.dtactics[i].key
            tactics = atactics + '|' + dtactics

            combatDetails = $scope.combatDetails.replace(new RegExp(r1.join('|'), 'g'), (m) ->
              r2[r1.indexOf(m)]
            )
            atkShips = findShips($scope.attackerDetails)
            dfdShips = findShips($scope.defenderDetails)
            shipDetails = atkShips + '|||' + dfdShips

            $http.post 'api/battles/report/entry', {
              combatDetails: combatDetails, title: $scope.title, battleId: $scope.battleId, tactics: tactics,
              attackerName: $scope.attackerName, defenderName: $scope.defenderName, shipDetails: shipDetails
            }
            .success (data) ->
              $state.go('battleReport', {reportId: data})

        findShips = (string) ->
          ship = getMatches(string, /ship_battle_icon ship_(\d+)/g, 1)
          num = getMatches(string, /capital_ship_count">(\d+)/g, 1)
          val = ship.map (e, i) ->
            ship[i] + ',' + num[i]
          val.join('|')

        getMatches = (string, regex, index) ->
          matches = []
          match
          while(match = regex.exec(string))
            matches.push(match[index])
          matches

        escapeRegExp = (string) ->
          string.replace /([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1"

#        replaceAll = (string, find, replace) ->
#          string.replace new RegExp(escapeRegExp(find), 'g'), replace

  web.controller 'BattleReportCtrl',
    class BattleReportCtrl
      constructor: ($scope, $stateParams, battleReport) ->

        $scope.tacticAttacker = battleReport.attackerTactics
        $scope.tacticDefender = battleReport.defenderTactics
        $scope.fleetAttacker = battleReport.summary[0]
        $scope.fleetDefender = battleReport.summary[1]
        $scope.report = battleReport

  web.directive 'reportShip', ->
    restrict: 'E'
    templateUrl: '/vassets/partials/reportShip-tpl.html'
    transclude: true
    scope:
      ships: '='

  web.directive 'reportCombatant', ->
    restrict: 'E'
    templateUrl: '/vassets/partials/reportCombatant-tpl.html'
    transclude: true
    scope:
      combatant: '='

  web.directive 'reportTactics', ->
    restrict: 'E'
    templateUrl: '/vassets/partials/reportTactics-tpl.html'
    transclude: true
    scope:
      tactics: '='

  web.directive 'reportSummary', ->
    restrict: 'E'
    templateUrl: '/vassets/partials/reportSummary-tpl.html'
    transclude: true
    scope:
      ships: '='

  web.directive 'shipAttack', ->
    restrict: 'E'
    templateUrl: '/vassets/partials/shipAttack-tpl.html'
    transclude: true
    scope:
      attack: '='

  web.directive 'shipDefense', ->
    restrict: 'E'
    templateUrl: '/vassets/partials/shipDefense-tpl.html'
    transclude: true
    scope:
      defense: '='

  web.directive 'shipDamage', ->
    restrict: 'E'
    templateUrl: '/vassets/partials/shipDamageAbsorb-tpl.html'
    transclude: true
    scope:
      ship: '='
)