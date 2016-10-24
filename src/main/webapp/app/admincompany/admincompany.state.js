/**
 * Created by Vladimir on 22.10.2016.
 */
(function() {
    'use strict';

    angular
        .module('truckCompanyApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('admincompany', {
            abstract: true,
            parent: 'app',
            views: {
                'content@': {
                    templateUrl: 'app/admincompany/admincompany.html',
                },
            }
        });
    }
})();
