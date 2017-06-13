(function () {
	'use strict';

	/**
	 * @ngdoc service
	 * @name eureka.phenotypes.PhenotypeService
	 * @description
	 * This service provides an API to interact with the REST endpoint for phenotypes.
	 * @requires $http
	 * @requires $q
	 */

	angular
			.module('eureka.phenotypes')
			.factory('PhenotypeService', PhenotypeService);

	PhenotypeService.$inject = ['$http', '$q', 'appProperties'];

	function PhenotypeService($http, $q, appProperties) {
		let {dataEndpoint} = appProperties;
		return ({
			getPhenotypeMessages: getPhenotypeMessages,
			getPhenotypeRoot: getPhenotypeRoot,
			createPhenotype: createPhenotype,
			getPhenotype: getPhenotype,
			updatePhenotype: updatePhenotype,
			removePhenotype: removePhenotype
		});

		function getPhenotypeMessages() {
			return {
				'CATEGORIZATION': {
					'displayName': 'Categorization',
					'description': `For defining a significant category of codes or clinical events or observations.`
				},
				'SEQUENCE': {
					'displayName': 'Sequence',
					'description': `For defining a disease, finding or patient care process to be reflected by codes,
                    clinical events and/or observations in a specified sequential temporal pattern.`
				},
				'FREQUENCY': {
					'displayName': 'Frequency',
					'description': `For defining a disease, finding or patient care process to be reflected by codes,
                    clinical events and/or observations in a specified frequency.`
				},
				'THRESHOLD': {
					'displayName': 'Threshold',
					'description': `For defining clinically significant thresholds on the value of an observation.`
				}
			};
		}

		function getPhenotypeRoot() {
			return $http.get(dataEndpoint + '/phenotypes?summarize=true')
					.then(handleSuccess, handleError);
		}

		function createPhenotype(newObject) {

			return $http.post(dataEndpoint + '/phenotypes', newObject)
					.then(handleSuccess, handleError);
		}

		function updatePhenotype(updateObject) {

			return $http.put(dataEndpoint + '/phenotypes/' + updateObject.id, updateObject)
					.then(handleSuccess, handleError);

		}

		function getPhenotype(key, summarize) {
			if (summarize === undefined) {
				summarize = false;
			}
			return $http.get(dataEndpoint + '/phenotypes/' + key + '?summarize=' + summarize)
					.then(handleSuccess, handleError);

		}

		function removePhenotype(id) {
			return $http['delete'](dataEndpoint + '/phenotypes/' + id)
					.then(handleSuccess, handleError);

		}

		function handleSuccess(response) {
			return response.data;
		}

		function handleError(response) {
			if (!angular.isObject(response.data) && !response.data) {
				if (response.statusText) {
					return ($q.reject(response.statusText));
				} else {
					return ($q.reject('The server may be down.'));
				}
			}
			return ($q.reject(response.data));
		}



	}

}());