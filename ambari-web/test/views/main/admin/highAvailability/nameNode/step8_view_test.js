/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


var App = require('app');
require('views/main/admin/highAvailability/nameNode/step8_view');

describe('App.HighAvailabilityWizardStep8View', function () {
  var view = App.HighAvailabilityWizardStep8View.create({
    controller: Em.Object.create({
      content: {},
      pullCheckPointStatus: Em.K
    })
  });

  describe("#step8BodyText", function() {
    it("", function() {
      view.set('controller.content.masterComponentHosts', [
        {
          isCurNameNode: true,
          hostName: 'host1'
        },
        {
          isAddNameNode: true,
          hostName: 'host2'
        }
      ]);
      view.set('controller.content.hdfsUser', 'user');
      view.propertyDidChange('step8BodyText');
      expect(view.get('step8BodyText')).to.equal(Em.I18n.t('admin.highAvailability.wizard.step8.body').format('user', 'host1', 'host2'));
    });
  });
});
