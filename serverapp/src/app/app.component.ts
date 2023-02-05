import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { BehaviorSubject, catchError, map, Observable, of, startWith } from 'rxjs';
import { DataState } from './enum/data-state.enum';
import { Status } from './enum/status.enum';
import { AppState } from './interface/app-state';
import { CustomResponse } from './interface/custom-response';
import { Server } from './interface/server';
import { NotificationService } from './service/notification.service';
import { ServerService } from './service/server.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent implements OnInit {
  readonly DataState = DataState;
  readonly Status = Status;
  private filterSubject = new BehaviorSubject<string>('');
  private dataSubject = new BehaviorSubject<CustomResponse>(null!);
  filterStatus$ = this.filterSubject.asObservable();

  private isLoading = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoading.asObservable();

  appState$: Observable<AppState<CustomResponse>> | undefined;
  constructor(
    private serverService: ServerService,
    private notifier:NotificationService
  ) { 

    }


  ngOnInit(): void {
    this.appState$ = this.serverService.server$.pipe(
      map(response => {
        this.dataSubject.next(response);
        this.notifier.onDefault(response.message);
        return {
          dataState: DataState.LOADED_STATE,
          appData: response
        }
      }),
      startWith({
        dataState: DataState.LOADING_STATE
      }),
      catchError(error => {
        this.notifier.onError(error);
        return of(
          {
            dataState: DataState.ERROR_STATE,
            error
          }
        )
      })
    );
  }




  pingServer(ipAddress: string): void {
    console.log("Pinging:", ipAddress);
    this.filterSubject.next(ipAddress);
    this.appState$ = this.serverService.ping$(ipAddress).pipe(
      map(response => {
        console.log("Map started: ", response);
        this.notifier.onDefault(response.message);
        if (this.dataSubject.value != null && response != null && response.data != null && response.data.server != null && response.data.server.id != null && this.dataSubject.value.data.servers != null) {
          this.dataSubject.value.data.servers[
            this.dataSubject.value.data.servers.findIndex(server =>
              server.id == response.data.server?.id
            )
          ] = response.data.server;
          console.log("Map finished: ", response.data.server);
        } else {
          console.log("Oups, map went wrong");
        }

        this.filterSubject.next('');
        return {
          dataState: DataState.LOADED_STATE,
          appData: this.dataSubject.value
        }
      }),
      startWith({
        dataState: DataState.LOADED_STATE,
        appData: this.dataSubject.value
      }),
      catchError(error => {
        console.log("You error!");
        this.notifier.onError(error);
        this.filterSubject.next('');
        return of(
          {
            dataState: DataState.ERROR_STATE,
            error
          }
        )
      })
    );
  }


  filterServers(event: any): void {
    let status:Status = event.target.value;
    console.log("filtring:", status);
    this.appState$ = this.serverService.filter$(status, this.dataSubject.value).pipe(
      map(response => {
        this.notifier.onDefault(response.message);
        return {
          dataState: DataState.LOADED_STATE,
          appData: response
        }
      }),
      startWith({
        dataState: DataState.LOADED_STATE,
        appData: this.dataSubject.value
      }),
      catchError(error => {
        console.log("You error!");
        this.notifier.onError(error);
        this.filterSubject.next('');
        return of(
          {
            dataState: DataState.ERROR_STATE,
            error
          }
        )
      })
    );
  }



  saveServer(serverForm: NgForm): void {
    console.log("saving:", serverForm);
    this.isLoading.next(true);

    this.appState$ = this.serverService.save$(serverForm.value as Server).pipe(
      map(response => {
        this.notifier.onDefault(response.message);
        let currentList:Server[];
        if(this.dataSubject.value.data.servers==undefined) {
          currentList = [];
        }else{
          currentList = this.dataSubject.value.data.servers;
        }
        let addedServer:Server;
        if(response.data.server!=undefined){
          addedServer = response.data.server;
        }else{
          addedServer = {
            id : 0,
            ipAddress :  "0.0.0",
            name : "wrong",
            memory : "0bits",
            type : "none",
            imageUrl : 'unknown',
            status : Status.SERVER_DOWN
          };
        }
        this.dataSubject.next(
          {
            ...response, 
            data:{servers:[addedServer, ...currentList]}
          }
        )
        document.getElementById('closeModal')?.click();
        serverForm.resetForm({status:this.Status.SERVER_DOWN});
        this.isLoading.next(false);
        return {
          dataState: DataState.LOADED_STATE,
          appData: this.dataSubject.value
        }
      }),
      startWith({
        dataState: DataState.LOADED_STATE,
        appData: this.dataSubject.value
      }),
      catchError(error => {
        console.log("You error!");
        this.notifier.onError(error);
        this.isLoading.next(false);
        return of(
          {
            dataState: DataState.ERROR_STATE,
            error
          }
        )
      })
    );
  }




  

  deleteServer(server: Server): void {
    console.log("delete:", server.ipAddress);
    this.appState$ = this.serverService.delete$(server.id).pipe(
      map(response => {
        this.notifier.onDefault(response.message);
        console.log("deleted: ", response);
        let newList:Server[] = [];
        if(this.dataSubject.value.data.servers!=undefined)
          newList = this.dataSubject.value.data.servers.filter(e=>e.id!=server.id);
        this.dataSubject.next({
          ...response,
          data: {
            servers: newList
          }
        });
        return {
          dataState: DataState.LOADED_STATE,
          appData: this.dataSubject.value
        }
      }),
      startWith({
        dataState: DataState.LOADED_STATE,
        appData: this.dataSubject.value
      }),
      catchError(error => {
        console.log("You error!");
        this.notifier.onError(error);
        return of(
          {
            dataState: DataState.ERROR_STATE,
            error
          }
        )
      })
    );
  }

  printReport():void{
    let dataType = 'application/vnd.ms-excel.sheet.macroEnabled.12';
    let tableSelect = document.getElementById('servers');
    let tableHtml = tableSelect?.outerHTML.replace(/ /g, '%20');
    let downloadLink = document.createElement('a');
    document.body.appendChild(downloadLink);
    downloadLink.href = 'data:'+dataType+', '+tableHtml;
    downloadLink.download = 'server-report.xls';
    downloadLink.click();
    document.body.removeChild(downloadLink);    
  }
}
