# Development status
Development Status values :
* TODO (Nothing has been done but will be done in near future)
* In progress (in devlopment progress)
* Done (works and tested)
* Problem (done but not tested or have bugs)

## Tasks
<table>
    <th>task name</th>
    <th>comment (and related issues)</th>
    <th>progress status</th>
    
    <tr>
        <td>Init(opts)</td>
        <td>Called when erocci start, feature#3</td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Terminate()</td>
        <td>Exit the application, feature#4</td>
        <td>Done</td>
    </tr>
    <tr>
        <td>SaveResource(id, ...)</td>
        <td>Save a resource and add it to Configuration model, feature#5</td>
        <td>Done</td>
    </tr> 
    <tr>
        <td>SaveLink(id, ...)</td>
        <td>Save a link and add it to Configuration model, feature #6</td>
        <td>Done</td>
    </tr>
        <td>Update(id, ...)</td>
        <td>Update attributes of an entity, feature #7</td>
        <td>Done</td>
    <tr>
    </tr>
        <td>SaveMixin(id, entities)</td>
        <td>Associate a list of entities with a mixin, replacing existing list if any previously given, feature #8</td>
        <td>Problem</td>
    </tr>
    <tr>
        <td>UpdateMixin(id, entities)</td>
        <td>Associate a list of entities with a mixin, replacing existing list if any previously given, feature #9</td>
        <td>Problem</td>
    </tr>
    <tr>
        <td>Find(id)</td>
        <td>Find an entity by his relative path, feature #10</td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Load(id)</td>
        <td>Describe an entity contents, feature #11</td>
        <td>Done</td>
    </tr>
    <tr>
        <td>List(id, filters)</td>
        <td>Get an iterator for a collection: then use Next() to iterate, feature #12</td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Next(opaque_id, start, items)</td>
        <td>Retrieve items for a collection, from start(int) with number items, feature #13</td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Delete(id)</td>
        <td>Remove entity or dissociate mixin from configuration, feature #14</td>
        <td>Done</td>
    </tr>
    <tr>
        <td>property schema (core interface)</td>
        <td>Assign an OCCI extension xml, on load, for now, extension are loaded via ConfigurationManager , for an owner and a Configuration Object, feature #15</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>AddMixin(id, location, owner)</td>
        <td>Add a user mixin, act as a user tag, feature #16</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>DelMixin(id, location, owner)</td>
        <td>Delete a user mixin, feature #17</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Action(id, action_id, attributes)</td>
        <td>Execute Action command for real interaction, action is call properly, while action has no connector, it use OCL validator but cant launch a command, feature #18</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Cloud connector</td>
        <td>Connector based on CloudDesigner implementation, for multi cloud purpose</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Docker connector</td>
        <td>onnector based on CloudDesigner implementation, for Docker container purpose</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Hypervisor connector</td>
        <td>Connector based on CloudDesigner implementation, for Hypervisor (like xen, vmware etc.) purpose</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Simulator connector</td>
        <td>Connector based on CloudDesigner implementation, for Simulator purpose</td>
        <td>TODO</td>
    </tr>

</table>


