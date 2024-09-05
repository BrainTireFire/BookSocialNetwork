/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { findAllFeedbackByBook } from '../fn/feed-back/find-all-feedback-by-book';
import { FindAllFeedbackByBook$Params } from '../fn/feed-back/find-all-feedback-by-book';
import { PagedResponseFeedbackResponse } from '../models/paged-response-feedback-response';
import { saveFeedBack } from '../fn/feed-back/save-feed-back';
import { SaveFeedBack$Params } from '../fn/feed-back/save-feed-back';


/**
 * FeedBack API
 */
@Injectable({ providedIn: 'root' })
export class FeedBackService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `saveFeedBack()` */
  static readonly SaveFeedBackPath = '/feedbacks';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `saveFeedBack()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  saveFeedBack$Response(params: SaveFeedBack$Params, context?: HttpContext): Observable<StrictHttpResponse<number>> {
    return saveFeedBack(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `saveFeedBack$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  saveFeedBack(params: SaveFeedBack$Params, context?: HttpContext): Observable<number> {
    return this.saveFeedBack$Response(params, context).pipe(
      map((r: StrictHttpResponse<number>): number => r.body)
    );
  }

  /** Path part for operation `findAllFeedbackByBook()` */
  static readonly FindAllFeedbackByBookPath = '/feedbacks/book/{book-id}';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `findAllFeedbackByBook()` instead.
   *
   * This method doesn't expect any request body.
   */
  findAllFeedbackByBook$Response(params: FindAllFeedbackByBook$Params, context?: HttpContext): Observable<StrictHttpResponse<PagedResponseFeedbackResponse>> {
    return findAllFeedbackByBook(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `findAllFeedbackByBook$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findAllFeedbackByBook(params: FindAllFeedbackByBook$Params, context?: HttpContext): Observable<PagedResponseFeedbackResponse> {
    return this.findAllFeedbackByBook$Response(params, context).pipe(
      map((r: StrictHttpResponse<PagedResponseFeedbackResponse>): PagedResponseFeedbackResponse => r.body)
    );
  }

}
